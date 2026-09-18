package com.cyclosa.workflow.scheduler;

import com.cyclosa.employee.entity.EmployeeEmploymentInfo;
import com.cyclosa.employee.repository.EmployeeEmploymentInfoRepository;
import com.cyclosa.workflow.entity.WorkflowApprovalStep;
import com.cyclosa.workflow.entity.WorkflowInstance;
import com.cyclosa.workflow.entity.WorkflowStep;
import com.cyclosa.workflow.enums.ApprovalStatus;
import com.cyclosa.workflow.enums.OverdueAction;
import com.cyclosa.workflow.enums.WorkflowAction;
import com.cyclosa.workflow.event.WorkflowCompletedEvent;
import com.cyclosa.workflow.repository.WorkflowApprovalStepRepository;
import com.cyclosa.workflow.repository.WorkflowInstanceRepository;
import com.cyclosa.workflow.repository.WorkflowStepRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class WorkflowSlaScheduler {

    private final WorkflowApprovalStepRepository approvalStepRepository;
    private final WorkflowInstanceRepository instanceRepository;
    private final WorkflowStepRepository stepRepository;
    private final EmployeeEmploymentInfoRepository employmentInfoRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Chạy định kỳ mỗi 15 phút quét các bước phê duyệt bị quá hạn SLA.
     */
    @Scheduled(cron = "0 */15 * * * *")
    @Transactional
    public void processOverdueWorkflowSteps() {
        LocalDateTime now = LocalDateTime.now();
        List<WorkflowApprovalStep> overdueSteps = approvalStepRepository.findOverduePendingSteps(now);

        if (overdueSteps.isEmpty()) {
            return;
        }

        log.info("Phát hiện {} bước phê duyệt bị quá hạn SLA tại mốc {}", overdueSteps.size(), now);

        for (WorkflowApprovalStep step : overdueSteps) {
            try {
                processSingleOverdueStep(step, now);
            } catch (Exception e) {
                log.error("Lỗi khi xử lý quá hạn cho approval step ID: {}: {}", step.getId(), e.getMessage(), e);
            }
        }
    }

    private void processSingleOverdueStep(WorkflowApprovalStep approvalStep, LocalDateTime now) {
        WorkflowStep stepDef = stepRepository.findById(approvalStep.getStepId()).orElse(null);
        OverdueAction action = stepDef != null && stepDef.getOverdueAction() != null
                ? stepDef.getOverdueAction()
                : OverdueAction.REMIND;

        WorkflowInstance instance = approvalStep.getWorkflowInstance();

        switch (action) {
            case REMIND -> {
                log.warn("SLA REMINDER: Bước '{}' (Instance ID: {}) của nhân viên {} đã quá hạn vào lúc {}",
                        approvalStep.getStepName(), instance.getId(),
                        approvalStep.getAssignedApproverEmployeeId(), approvalStep.getSlaDeadlineAt());
                // Khi tích hợp Module 19, sẽ gửi notification qua event
            }

            case AUTO_ESCALATE -> {
                log.info("SLA AUTO_ESCALATE: Đang leo cấp bước '{}' (Instance ID: {})",
                        approvalStep.getStepName(), instance.getId());

                if (approvalStep.getAssignedApproverEmployeeId() != null) {
                    Optional<EmployeeEmploymentInfo> managerInfo = employmentInfoRepository
                            .findByEmployeeId(approvalStep.getAssignedApproverEmployeeId());

                    if (managerInfo.isPresent() && managerInfo.get().getManagerEmployeeId() != null) {
                        // Chuyển sang quản lý cấp cao hơn
                        approvalStep.setAssignedApproverEmployeeId(managerInfo.get().getManagerEmployeeId());
                        // Gia hạn thêm SLA
                        if (stepDef != null && stepDef.getSlaHours() != null) {
                            approvalStep.setSlaDeadlineAt(now.plusHours(stepDef.getSlaHours()));
                        }
                        approvalStepRepository.save(approvalStep);
                        log.info("Đã leo cấp thành công lên approver mới: {}", managerInfo.get().getManagerEmployeeId());
                        return;
                    }
                }
                // Nếu không tìm được cấp trên để leo -> fallback sang Remind
                log.warn("Không tìm được cấp trên của approver {}, giữ nguyên bước",
                        approvalStep.getAssignedApproverEmployeeId());
            }

            case AUTO_REJECT -> {
                log.info("SLA AUTO_REJECT: Tự động từ chối đơn do quá hạn cho instance ID: {}", instance.getId());
                approvalStep.setAction(WorkflowAction.AUTO_REJECTED);
                approvalStep.setActedAt(now);
                approvalStep.setComment("Tự động từ chối do quá hạn xử lý (SLA)");
                approvalStepRepository.save(approvalStep);

                instance.setStatus(ApprovalStatus.REJECTED);
                instance.setCurrentStepId(null);
                instanceRepository.save(instance);

                eventPublisher.publishEvent(WorkflowCompletedEvent.builder()
                        .workflowInstanceId(instance.getId())
                        .companyId(instance.getCompanyId())
                        .requestType(instance.getRequestType())
                        .requestId(instance.getRequestId())
                        .requesterEmployeeId(instance.getRequesterEmployeeId())
                        .finalStatus(ApprovalStatus.REJECTED)
                        .reason("Tự động từ chối do quá hạn xử lý SLA")
                        .build());
            }
        }
    }
}
