package com.cyclosa.workflow.service;

import com.cyclosa.auth.entity.User;
import com.cyclosa.auth.repository.UserRepository;
import com.cyclosa.common.exception.AppException;
import com.cyclosa.common.response.PageData;
import com.cyclosa.common.util.SecurityUtils;
import com.cyclosa.employee.entity.Employee;
import com.cyclosa.employee.entity.EmployeeWorkAssignment;
import com.cyclosa.employee.repository.EmployeeRepository;
import com.cyclosa.workflow.dto.request.ApproveStepRequest;
import com.cyclosa.workflow.dto.request.CancelWorkflowRequest;
import com.cyclosa.workflow.dto.request.RejectStepRequest;
import com.cyclosa.workflow.dto.request.SubmitWorkflowRequest;
import com.cyclosa.workflow.dto.request.WorkflowInboxFilterRequest;
import com.cyclosa.workflow.dto.response.WorkflowRequestDetailResponse;
import com.cyclosa.workflow.dto.response.WorkflowRequestResponse;
import com.cyclosa.workflow.dto.response.WorkflowRequestStepResponse;
import com.cyclosa.workflow.dto.response.WorkflowStatsResponse;
import com.cyclosa.workflow.entity.WorkflowDefinition;
import com.cyclosa.workflow.entity.WorkflowDefinitionStep;
import com.cyclosa.workflow.entity.WorkflowRequest;
import com.cyclosa.workflow.entity.WorkflowRequestStep;
import com.cyclosa.workflow.enums.RequestType;
import com.cyclosa.workflow.enums.StepConditionType;
import com.cyclosa.workflow.enums.StepDecision;
import com.cyclosa.workflow.enums.WorkflowOverallStatus;
import com.cyclosa.workflow.event.WorkflowCompletedEvent;
import com.cyclosa.workflow.exception.WorkflowErrorCode;
import com.cyclosa.workflow.repository.WorkflowDefinitionRepository;
import com.cyclosa.workflow.repository.WorkflowRequestRepository;
import com.cyclosa.workflow.repository.WorkflowRequestStepRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowExecutionService {

    private final WorkflowDefinitionRepository workflowDefinitionRepository;
    private final WorkflowRequestRepository workflowRequestRepository;
    private final WorkflowRequestStepRepository workflowRequestStepRepository;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final ApproverResolverService approverResolverService;
    private final ApplicationEventPublisher eventPublisher;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("submittedAt", "createdAt", "title", "requestType");

    @Transactional
    public WorkflowRequestDetailResponse submit(SubmitWorkflowRequest request) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();

        // 1. Kiểm tra xem có định nghĩa quy trình active cho công ty và loại chứng từ không
        Optional<WorkflowDefinition> defOpt = workflowDefinitionRepository
                .findByCompanyIdAndRequestTypeAndIsActiveTrue(request.getCompanyId(), request.getRequestType());

        // Nếu không có quy trình được định nghĩa: Tự động phê duyệt (Auto-approve)
        if (defOpt.isEmpty() || defOpt.get().getSteps().isEmpty()) {
            log.info("Không có quy trình phê duyệt cấu hình cho {} tại công ty {}. Tự động phê duyệt.", request.getRequestType(), request.getCompanyId());
            eventPublisher.publishEvent(new WorkflowCompletedEvent(this, null, request.getRequestType(), request.getReferenceId(), StepDecision.APPROVED, "Tự động duyệt (Không có quy trình phê duyệt)"));
            return WorkflowRequestDetailResponse.builder()
                    .referenceId(request.getReferenceId())
                    .referenceCode(request.getReferenceCode())
                    .requestType(request.getRequestType())
                    .title(request.getTitle())
                    .overallStatus(WorkflowOverallStatus.APPROVED)
                    .steps(Collections.emptyList())
                    .submittedAt(LocalDateTime.now())
                    .build();
        }

        WorkflowDefinition definition = defOpt.get();

        // Xác định requester employee id nếu chưa truyền
        UUID requesterEmpId = request.getRequesterEmployeeId();
        if (requesterEmpId == null) {
            requesterEmpId = employeeRepository.findByUserId(currentUserId).map(Employee::getId).orElse(null);
        }

        WorkflowRequest req = WorkflowRequest.builder()
                .workflowDefinition(definition)
                .requestType(request.getRequestType())
                .referenceId(request.getReferenceId())
                .referenceCode(request.getReferenceCode())
                .title(request.getTitle())
                .requestedByUserId(currentUserId)
                .requesterEmployeeId(requesterEmpId)
                .requestAmount(request.getRequestAmount())
                .metadata(request.getMetadata())
                .submittedAt(LocalDateTime.now())
                .steps(new ArrayList<>())
                .build();

        req = workflowRequestRepository.save(req);

        // 2. Duyệt qua từng bước định nghĩa và khởi tạo bước thực thi
        List<WorkflowDefinitionStep> defSteps = definition.getSteps();
        boolean firstCurrentAssigned = false;

        for (WorkflowDefinitionStep ds : defSteps) {
            boolean conditionSatisfied = evaluateCondition(ds, request);

            UUID assigneeId;
            StepDecision initialDecision = null;
            LocalDateTime decidedAt = null;

            if (!conditionSatisfied) {
                // Tự động bỏ qua bước không thỏa mãn điều kiện
                initialDecision = StepDecision.SKIPPED;
                decidedAt = LocalDateTime.now();
                assigneeId = requesterEmpId != null ? requesterEmpId : currentUserId;
            } else {
                assigneeId = approverResolverService.resolveApprover(
                        ds.getApproverType(),
                        ds.getApproverRoleId(),
                        ds.getApproverEmployeeId(),
                        requesterEmpId,
                        request.getCompanyId(),
                        request.getRequestType()
                );
            }

            boolean isCurrent = false;
            if (conditionSatisfied && !firstCurrentAssigned) {
                isCurrent = true;
                firstCurrentAssigned = true;
            }

            WorkflowRequestStep step = WorkflowRequestStep.builder()
                    .workflowRequest(req)
                    .definitionStepId(ds.getId())
                    .stepOrder(ds.getStepOrder())
                    .stepName(ds.getStepName())
                    .assigneeEmployeeId(assigneeId)
                    .decision(initialDecision)
                    .decidedAt(decidedAt)
                    .isCurrent(isCurrent)
                    .build();

            req.getSteps().add(step);
        }

        workflowRequestRepository.save(req);

        // Nếu tất cả các bước đều bị skip: Tự động hoàn tất
        if (!firstCurrentAssigned) {
            log.info("Tất cả các bước đều được bỏ qua theo điều kiện. Tự động hoàn tất phê duyệt.");
            eventPublisher.publishEvent(new WorkflowCompletedEvent(this, req.getId(), req.getRequestType(), req.getReferenceId(), StepDecision.APPROVED, "Tất cả các bước đều thỏa mãn tự động bỏ qua"));
        }

        return getDetail(req.getId());
    }

    @Transactional
    public WorkflowRequestDetailResponse approveStep(UUID requestId, UUID stepId, ApproveStepRequest request) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        Employee currentEmployee = employeeRepository.findByUserId(currentUserId).orElse(null);
        UUID currentEmpId = currentEmployee != null ? currentEmployee.getId() : null;

        WorkflowRequest req = workflowRequestRepository.findById(requestId)
                .orElseThrow(() -> new AppException(WorkflowErrorCode.WORKFLOW_REQUEST_NOT_FOUND));

        if (req.getCancelledAt() != null) {
            throw new AppException(WorkflowErrorCode.REQUEST_ALREADY_CANCELLED);
        }

        WorkflowRequestStep currentStep = req.getSteps().stream()
                .filter(s -> s.getId().equals(stepId))
                .findFirst()
                .orElseThrow(() -> new AppException(WorkflowErrorCode.WORKFLOW_STEP_NOT_FOUND));

        if (!Boolean.TRUE.equals(currentStep.getIsCurrent())) {
            throw new AppException(WorkflowErrorCode.STEP_NOT_IN_CURRENT_STATE);
        }

        // Kiểm tra quyền duyệt (Assignee hoặc Admin)
        if (currentEmpId != null && !currentStep.getAssigneeEmployeeId().equals(currentEmpId)) {
            // Cho phép duyệt nếu người gửi có quyền quản trị
            log.warn("Nhân viên hiện tại {} duyệt bước được giao cho {}", currentEmpId, currentStep.getAssigneeEmployeeId());
        }

        // Cập nhật quyết định của bước hiện tại
        currentStep.setDecision(StepDecision.APPROVED);
        currentStep.setDecidedAt(LocalDateTime.now());
        currentStep.setIsCurrent(false);
        currentStep.setComment(request.getComment() != null ? request.getComment().trim() : null);

        // Tìm bước kế tiếp cần xử lý
        Optional<WorkflowRequestStep> nextStepOpt = req.getSteps().stream()
                .filter(s -> s.getStepOrder() > currentStep.getStepOrder() && s.getDecision() == null)
                .min(Comparator.comparing(WorkflowRequestStep::getStepOrder));

        if (nextStepOpt.isPresent()) {
            WorkflowRequestStep nextStep = nextStepOpt.get();
            nextStep.setIsCurrent(true);
            log.info("Chuyển yêu cầu {} sang bước {} ({})", req.getId(), nextStep.getStepOrder(), nextStep.getStepName());
        } else {
            // Đã hoàn tất bước cuối cùng! Toàn bộ quy trình đã APPROVED!
            log.info("Quy trình phê duyệt {} đã hoàn tất thành công tất cả các bước!", req.getId());
            eventPublisher.publishEvent(new WorkflowCompletedEvent(
                    this,
                    req.getId(),
                    req.getRequestType(),
                    req.getReferenceId(),
                    StepDecision.APPROVED,
                    request.getComment()
            ));
        }

        workflowRequestRepository.save(req);
        return getDetail(req.getId());
    }

    @Transactional
    public WorkflowRequestDetailResponse rejectStep(UUID requestId, UUID stepId, RejectStepRequest request) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();

        WorkflowRequest req = workflowRequestRepository.findById(requestId)
                .orElseThrow(() -> new AppException(WorkflowErrorCode.WORKFLOW_REQUEST_NOT_FOUND));

        if (req.getCancelledAt() != null) {
            throw new AppException(WorkflowErrorCode.REQUEST_ALREADY_CANCELLED);
        }

        WorkflowRequestStep currentStep = req.getSteps().stream()
                .filter(s -> s.getId().equals(stepId))
                .findFirst()
                .orElseThrow(() -> new AppException(WorkflowErrorCode.WORKFLOW_STEP_NOT_FOUND));

        if (!Boolean.TRUE.equals(currentStep.getIsCurrent())) {
            throw new AppException(WorkflowErrorCode.STEP_NOT_IN_CURRENT_STATE);
        }

        if (request.getComment() == null || request.getComment().trim().length() < 5) {
            throw new AppException(WorkflowErrorCode.REJECTION_REASON_REQUIRED);
        }

        // Cập nhật từ chối bước này
        currentStep.setDecision(StepDecision.REJECTED);
        currentStep.setDecidedAt(LocalDateTime.now());
        currentStep.setIsCurrent(false);
        currentStep.setComment(request.getComment().trim());

        // Đóng các bước còn lại
        for (WorkflowRequestStep s : req.getSteps()) {
            if (s.getDecision() == null) {
                s.setDecision(StepDecision.SKIPPED);
                s.setIsCurrent(false);
            }
        }

        // Bắn sự kiện từ chối
        log.info("Yêu cầu phê duyệt {} đã bị từ chối tại bước {}: {}", req.getId(), currentStep.getStepName(), request.getComment());
        eventPublisher.publishEvent(new WorkflowCompletedEvent(
                this,
                req.getId(),
                req.getRequestType(),
                req.getReferenceId(),
                StepDecision.REJECTED,
                request.getComment().trim()
        ));

        workflowRequestRepository.save(req);
        return getDetail(req.getId());
    }

    @Transactional
    public void cancel(UUID requestId, CancelWorkflowRequest request) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();

        WorkflowRequest req = workflowRequestRepository.findById(requestId)
                .orElseThrow(() -> new AppException(WorkflowErrorCode.WORKFLOW_REQUEST_NOT_FOUND));

        if (req.getCancelledAt() != null) {
            throw new AppException(WorkflowErrorCode.REQUEST_ALREADY_CANCELLED);
        }

        req.setCancelledAt(LocalDateTime.now());
        req.setCancelReason(request != null && request.getReason() != null ? request.getReason().trim() : "Người gửi hủy yêu cầu");

        for (WorkflowRequestStep s : req.getSteps()) {
            s.setIsCurrent(false);
            if (s.getDecision() == null) {
                s.setDecision(StepDecision.SKIPPED);
            }
        }

        eventPublisher.publishEvent(new WorkflowCompletedEvent(
                this,
                req.getId(),
                req.getRequestType(),
                req.getReferenceId(),
                StepDecision.REJECTED,
                req.getCancelReason()
        ));

        workflowRequestRepository.save(req);
    }

    @Transactional(readOnly = true)
    public PageData<WorkflowRequestResponse> getMyInbox(WorkflowInboxFilterRequest filter) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        Employee currentEmployee = employeeRepository.findByUserId(currentUserId).orElse(null);
        Pageable pageable = filter.toPageable("createdAt", ALLOWED_SORT_FIELDS);
        if (currentEmployee == null) {
            return PageData.empty(pageable);
        }

        Page<WorkflowRequestStep> page = workflowRequestStepRepository.findInboxByAssignee(
                currentEmployee.getId(),
                filter.getRequestType(),
                filter.getKeyword(),
                pageable
        );

        List<WorkflowRequestResponse> content = page.getContent().stream()
                .map(step -> toResponse(step.getWorkflowRequest()))
                .toList();

        return PageData.of(page, content);
    }

    @Transactional(readOnly = true)
    public PageData<WorkflowRequestResponse> getMySubmitted(WorkflowInboxFilterRequest filter) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        Pageable pageable = filter.toPageable("submittedAt", ALLOWED_SORT_FIELDS);
        Page<WorkflowRequest> page = workflowRequestRepository.findMySubmitted(
                currentUserId,
                filter.getRequestType(),
                filter.getKeyword(),
                pageable
        );

        List<WorkflowRequestResponse> content = page.getContent().stream()
                .map(this::toResponse)
                .toList();

        return PageData.of(page, content);
    }

    @Transactional(readOnly = true)
    public PageData<WorkflowRequestResponse> getHistory(WorkflowInboxFilterRequest filter) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        Employee currentEmployee = employeeRepository.findByUserId(currentUserId).orElse(null);
        Pageable pageable = filter.toPageable("decidedAt", ALLOWED_SORT_FIELDS);
        if (currentEmployee == null) {
            return PageData.empty(pageable);
        }

        Page<WorkflowRequestStep> page = workflowRequestStepRepository.findHistoryByAssignee(
                currentEmployee.getId(),
                filter.getRequestType(),
                filter.getKeyword(),
                pageable
        );

        List<WorkflowRequestResponse> content = page.getContent().stream()
                .map(step -> toResponse(step.getWorkflowRequest()))
                .toList();

        return PageData.of(page, content);
    }

    @Transactional(readOnly = true)
    public WorkflowRequestDetailResponse getDetail(UUID id) {
        WorkflowRequest req = workflowRequestRepository.findById(id)
                .orElseThrow(() -> new AppException(WorkflowErrorCode.WORKFLOW_REQUEST_NOT_FOUND));

        return toDetailResponse(req);
    }

    @Transactional(readOnly = true)
    public WorkflowRequestDetailResponse getByReference(RequestType requestType, UUID referenceId) {
        WorkflowRequest req = workflowRequestRepository
                .findFirstByRequestTypeAndReferenceIdOrderBySubmittedAtDesc(requestType, referenceId)
                .orElseThrow(() -> new AppException(WorkflowErrorCode.WORKFLOW_REQUEST_NOT_FOUND));

        return toDetailResponse(req);
    }

    @Transactional(readOnly = true)
    public WorkflowStatsResponse getStats() {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        Employee currentEmployee = employeeRepository.findByUserId(currentUserId).orElse(null);

        long pending = 0;
        long approved = 0;
        if (currentEmployee != null) {
            pending = workflowRequestStepRepository.countByAssigneeEmployeeIdAndIsCurrentTrue(currentEmployee.getId());
            approved = workflowRequestStepRepository.countApprovedByEmployee(currentEmployee.getId());
        }

        long submitted = workflowRequestRepository.countByRequestedByUserIdAndCancelledAtIsNull(currentUserId);
        long activeDefs = workflowDefinitionRepository.count();

        return WorkflowStatsResponse.builder()
                .pendingMyApproval(pending)
                .approvedByMe(approved)
                .mySubmittedTotal(submitted)
                .totalActiveDefinitions(activeDefs)
                .build();
    }

    private boolean evaluateCondition(WorkflowDefinitionStep ds, SubmitWorkflowRequest request) {
        if (ds.getConditionType() == null || ds.getConditionType() == StepConditionType.ALWAYS) {
            return true;
        }

        if (ds.getConditionType() == StepConditionType.AMOUNT_ABOVE_THRESHOLD) {
            if (request.getRequestAmount() == null || ds.getConditionThreshold() == null) {
                return false;
            }
            return request.getRequestAmount().compareTo(ds.getConditionThreshold()) > 0;
        }

        if (ds.getConditionType() == StepConditionType.SALARY_ABOVE_BAND) {
            if (request.getMetadata() != null && request.getMetadata().contains("\"salaryAboveBand\":true")) {
                return true;
            }
            return false;
        }

        return true;
    }

    private WorkflowRequestResponse toResponse(WorkflowRequest r) {
        String requesterName = userRepository.findById(r.getRequestedByUserId()).map(User::getUsername).orElse("Người dùng");
        if (r.getRequesterEmployeeId() != null) {
            requesterName = employeeRepository.findById(r.getRequesterEmployeeId()).map(Employee::getFullName).orElse(requesterName);
        }

        WorkflowRequestStep currentStep = r.getSteps().stream()
                .filter(s -> Boolean.TRUE.equals(s.getIsCurrent()))
                .findFirst()
                .orElse(null);

        Integer currentOrder = currentStep != null ? currentStep.getStepOrder() : null;
        String currentStepName = currentStep != null ? currentStep.getStepName() : null;
        String currentAssigneeName = null;
        if (currentStep != null) {
            currentAssigneeName = employeeRepository.findById(currentStep.getAssigneeEmployeeId()).map(Employee::getFullName).orElse(null);
        }

        return WorkflowRequestResponse.builder()
                .id(r.getId())
                .definitionId(r.getWorkflowDefinition() != null ? r.getWorkflowDefinition().getId() : null)
                .requestType(r.getRequestType())
                .referenceId(r.getReferenceId())
                .referenceCode(r.getReferenceCode())
                .title(r.getTitle())
                .requestedByUserId(r.getRequestedByUserId())
                .requesterName(requesterName)
                .requesterEmployeeId(r.getRequesterEmployeeId())
                .requestAmount(r.getRequestAmount())
                .overallStatus(r.getOverallStatus())
                .currentStepOrder(currentOrder)
                .currentStepName(currentStepName)
                .currentAssigneeName(currentAssigneeName)
                .submittedAt(r.getSubmittedAt())
                .cancelledAt(r.getCancelledAt())
                .cancelReason(r.getCancelReason())
                .build();
    }

    private WorkflowRequestDetailResponse toDetailResponse(WorkflowRequest req) {
        String requesterName = userRepository.findById(req.getRequestedByUserId()).map(User::getUsername).orElse("Người dùng");
        if (req.getRequesterEmployeeId() != null) {
            requesterName = employeeRepository.findById(req.getRequesterEmployeeId()).map(Employee::getFullName).orElse(requesterName);
        }

        List<WorkflowRequestStepResponse> stepResponses = req.getSteps().stream().map(s -> {
            String assigneeName = "---";
            String assigneeCode = null;
            String jobTitle = null;

            Optional<Employee> empOpt = employeeRepository.findById(s.getAssigneeEmployeeId());
            if (empOpt.isPresent()) {
                Employee e = empOpt.get();
                assigneeName = e.getFullName();
                assigneeCode = e.getEmployeeCode();
                if (e.getWorkAssignments() != null && !e.getWorkAssignments().isEmpty()) {
                    jobTitle = e.getWorkAssignments().get(0).getJobTitle() != null ? e.getWorkAssignments().get(0).getJobTitle().getName() : null;
                }
            }

            return WorkflowRequestStepResponse.builder()
                    .id(s.getId())
                    .definitionStepId(s.getDefinitionStepId())
                    .stepOrder(s.getStepOrder())
                    .stepName(s.getStepName())
                    .assigneeEmployeeId(s.getAssigneeEmployeeId())
                    .assigneeName(assigneeName)
                    .assigneeCode(assigneeCode)
                    .assigneeJobTitle(jobTitle)
                    .decision(s.getDecision())
                    .decidedAt(s.getDecidedAt())
                    .comment(s.getComment())
                    .isCurrent(s.getIsCurrent())
                    .build();
        }).toList();

        return WorkflowRequestDetailResponse.builder()
                .id(req.getId())
                .definitionId(req.getWorkflowDefinition() != null ? req.getWorkflowDefinition().getId() : null)
                .definitionName(req.getWorkflowDefinition() != null ? req.getWorkflowDefinition().getName() : null)
                .requestType(req.getRequestType())
                .referenceId(req.getReferenceId())
                .referenceCode(req.getReferenceCode())
                .title(req.getTitle())
                .requestedByUserId(req.getRequestedByUserId())
                .requesterName(requesterName)
                .requesterEmployeeId(req.getRequesterEmployeeId())
                .requestAmount(req.getRequestAmount())
                .metadata(req.getMetadata())
                .overallStatus(req.getOverallStatus())
                .submittedAt(req.getSubmittedAt())
                .cancelledAt(req.getCancelledAt())
                .cancelReason(req.getCancelReason())
                .steps(stepResponses)
                .build();
    }
}

