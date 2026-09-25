package com.cyclosa.notification.listener;

import com.cyclosa.auth.entity.User;
import com.cyclosa.auth.service.UserService;
import com.cyclosa.notification.enums.NotificationType;
import com.cyclosa.notification.service.NotificationService;
import com.cyclosa.workflow.enums.ApprovalStatus;
import com.cyclosa.workflow.event.WorkflowCompletedEvent;
import com.cyclosa.workflow.event.WorkflowStepAssignedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class WorkflowNotificationEventListener {

    private final NotificationService notificationService;
    private final UserService userService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onWorkflowStepAssigned(WorkflowStepAssignedEvent event) {
        log.info("[NOTIFICATION] Bắt sự kiện WorkflowStepAssignedEvent cho stepId={}, approverEmployeeId={}",
                event.getStepId(), event.getAssignedApproverEmployeeId());

        if (event.getAssignedApproverEmployeeId() == null) {
            return;
        }

        Optional<User> approverUserOpt = userService.findByEmployeeId(event.getAssignedApproverEmployeeId());
        if (approverUserOpt.isEmpty()) {
            log.info("[NOTIFICATION] Nhân viên duyệt {} chưa có tài khoản User, bỏ qua gửi thông báo in-app.",
                    event.getAssignedApproverEmployeeId());
            return;
        }

        User approverUser = approverUserOpt.get();
        String title = "Yêu cầu cần phê duyệt: " + event.getRequestType();
        String content = String.format("Bạn được giao phê duyệt bước '%s' cho yêu cầu %s.",
                event.getStepName(), event.getRequestType());

        notificationService.send(
                approverUser.getId(),
                title,
                content,
                NotificationType.WORKFLOW,
                "/workflows/pending",
                "WORKFLOW_INSTANCE",
                event.getWorkflowInstanceId()
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onWorkflowCompleted(WorkflowCompletedEvent event) {
        log.info("[NOTIFICATION] Bắt sự kiện WorkflowCompletedEvent cho instanceId={}, status={}",
                event.getWorkflowInstanceId(), event.getFinalStatus());

        if (event.getRequesterEmployeeId() == null) {
            return;
        }

        Optional<User> requesterUserOpt = userService.findByEmployeeId(event.getRequesterEmployeeId());
        if (requesterUserOpt.isEmpty()) {
            log.info("[NOTIFICATION] Người yêu cầu {} chưa có tài khoản User, bỏ qua gửi thông báo in-app.",
                    event.getRequesterEmployeeId());
            return;
        }

        User requesterUser = requesterUserOpt.get();
        String statusLabel = event.getFinalStatus() == ApprovalStatus.APPROVED ? "Đã được phê duyệt"
                : (event.getFinalStatus() == ApprovalStatus.REJECTED ? "Bị từ chối" : "Đã kết thúc");

        String title = String.format("[%s] Yêu cầu %s", statusLabel, event.getRequestType());
        StringBuilder contentBuilder = new StringBuilder();
        contentBuilder.append(String.format("Yêu cầu %s của bạn đã %s.",
                event.getRequestType(), statusLabel.toLowerCase()));

        if (event.getReason() != null && !event.getReason().isBlank()) {
            contentBuilder.append(" Lý do: ").append(event.getReason());
        }

        notificationService.send(
                requesterUser.getId(),
                title,
                contentBuilder.toString(),
                NotificationType.WORKFLOW,
                "/workflows/history",
                "WORKFLOW_INSTANCE",
                event.getWorkflowInstanceId()
        );
    }
}
