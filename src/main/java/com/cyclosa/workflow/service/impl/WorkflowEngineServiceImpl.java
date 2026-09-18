package com.cyclosa.workflow.service.impl;

import com.cyclosa.common.exception.AppException;
import com.cyclosa.common.response.PageData;
import com.cyclosa.employee.entity.Employee;
import com.cyclosa.employee.repository.EmployeeRepository;
import com.cyclosa.workflow.dto.request.ApprovalActionRequest;
import com.cyclosa.workflow.dto.request.StartWorkflowRequest;
import com.cyclosa.workflow.dto.response.*;
import com.cyclosa.workflow.entity.*;
import com.cyclosa.workflow.enums.*;
import com.cyclosa.workflow.event.WorkflowCompletedEvent;
import com.cyclosa.workflow.event.WorkflowStepAssignedEvent;
import com.cyclosa.workflow.exception.WorkflowErrorCode;
import com.cyclosa.workflow.mapper.WorkflowMapper;
import com.cyclosa.workflow.repository.*;
import com.cyclosa.workflow.service.ApproverResolverService;
import com.cyclosa.workflow.service.ConditionEvaluatorService;
import com.cyclosa.workflow.service.WorkflowEngineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowEngineServiceImpl implements WorkflowEngineService {

    private final WorkflowDefinitionRepository definitionRepository;
    private final WorkflowStepRepository stepRepository;
    private final WorkflowConditionRepository conditionRepository;
    private final WorkflowInstanceRepository instanceRepository;
    private final WorkflowApprovalStepRepository approvalStepRepository;
    private final WorkflowDelegateRepository delegateRepository;
    private final EmployeeRepository employeeRepository;
    private final ApproverResolverService approverResolver;
    private final ConditionEvaluatorService conditionEvaluator;
    private final WorkflowMapper workflowMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public WorkflowInstanceResponse startWorkflow(StartWorkflowRequest request) {
        log.info("Bắt đầu khởi tạo workflow cho requestType: {}, requestId: {}, requester: {}",
                request.getRequestType(), request.getRequestId(), request.getRequesterEmployeeId());

        WorkflowDefinition definition = definitionRepository.findByCompanyIdAndRequestTypeAndStatus(
                request.getCompanyId(), request.getRequestType(), WorkflowStatus.PUBLISHED)
                .orElseThrow(() -> new AppException(WorkflowErrorCode.NO_PUBLISHED_WORKFLOW));

        // Kiểm tra xem đã có instance nào đang pending cho requestId này chưa
        Optional<WorkflowInstance> existingPendingOpt = instanceRepository
                .findByRequestTypeAndRequestId(request.getRequestType(), request.getRequestId())
                .filter(i -> i.getStatus() == ApprovalStatus.PENDING);

        if (existingPendingOpt.isPresent()) {
            throw new AppException(WorkflowErrorCode.STEP_ALREADY_ACTED,
                    "Yêu cầu này hiện đang có một quy trình phê duyệt đang chờ xử lý");
        }

        List<WorkflowStep> allSteps = stepRepository.findByWorkflowDefinitionIdOrderByStepOrderAsc(definition.getId());
        if (allSteps.isEmpty()) {
            throw new AppException(WorkflowErrorCode.NO_PUBLISHED_WORKFLOW,
                    "Quy trình đã xuất bản nhưng không có bước phê duyệt nào");
        }

        // Xác định bước đầu tiên (kiểm tra điều kiện điểm vào entry condition nếu có)
        WorkflowStep firstStep = resolveFirstStep(definition.getId(), allSteps, request.getContextVariables());

        // Tạo WorkflowInstance
        WorkflowInstance instance = WorkflowInstance.builder()
                .workflowDefinition(definition)
                .companyId(request.getCompanyId())
                .requestType(request.getRequestType())
                .requestId(request.getRequestId())
                .requesterEmployeeId(request.getRequesterEmployeeId())
                .status(ApprovalStatus.PENDING)
                .currentStepId(firstStep.getId())
                .build();

        WorkflowInstance savedInstance = instanceRepository.save(instance);

        // Resolve người duyệt cho bước đầu tiên
        UUID assignedApproverId = approverResolver.resolveAssignedApprover(
                firstStep, request.getRequesterEmployeeId(), request.getCompanyId());

        LocalDateTime slaDeadline = firstStep.getSlaHours() != null
                ? LocalDateTime.now().plusHours(firstStep.getSlaHours())
                : null;

        WorkflowApprovalStep approvalStep = WorkflowApprovalStep.builder()
                .workflowInstance(savedInstance)
                .stepId(firstStep.getId())
                .stepOrder(firstStep.getStepOrder())
                .stepName(firstStep.getName())
                .assignedApproverEmployeeId(assignedApproverId)
                .action(WorkflowAction.PENDING)
                .slaDeadlineAt(slaDeadline)
                .build();

        WorkflowApprovalStep savedApprovalStep = approvalStepRepository.save(approvalStep);
        savedInstance.getApprovalSteps().add(savedApprovalStep);

        // Phát event bước duyệt được giao
        eventPublisher.publishEvent(WorkflowStepAssignedEvent.builder()
                .workflowInstanceId(savedInstance.getId())
                .stepId(firstStep.getId())
                .stepName(firstStep.getName())
                .stepOrder(firstStep.getStepOrder())
                .requestType(savedInstance.getRequestType())
                .requestId(savedInstance.getRequestId())
                .requesterEmployeeId(savedInstance.getRequesterEmployeeId())
                .assignedApproverEmployeeId(assignedApproverId)
                .slaDeadlineAt(slaDeadline)
                .build());

        log.info("Khởi tạo thành công workflow instance ID: {}, bước 1: {} giao cho: {}",
                savedInstance.getId(), firstStep.getName(), assignedApproverId);

        return enrichInstanceResponse(savedInstance);
    }

    @Override
    @Transactional
    public WorkflowInstanceResponse approve(UUID instanceId, UUID currentEmployeeId, ApprovalActionRequest request) {
        WorkflowInstance instance = getInstanceOrThrow(instanceId);

        if (instance.getStatus() != ApprovalStatus.PENDING) {
            throw new AppException(WorkflowErrorCode.WORKFLOW_ALREADY_CLOSED);
        }

        WorkflowApprovalStep currentPendingStep = approvalStepRepository
                .findByWorkflowInstanceIdAndAction(instanceId, WorkflowAction.PENDING)
                .orElseThrow(() -> new AppException(WorkflowErrorCode.WORKFLOW_ALREADY_CLOSED));

        // Kiểm tra quyền duyệt
        boolean authorized = approverResolver.isAuthorizedApprover(
                currentPendingStep.getAssignedApproverEmployeeId(), currentEmployeeId, LocalDate.now());
        if (!authorized) {
            throw new AppException(WorkflowErrorCode.NOT_AUTHORIZED_APPROVER);
        }

        // Cập nhật bước hiện tại thành APPROVED
        currentPendingStep.setAction(WorkflowAction.APPROVED);
        currentPendingStep.setApproverEmployeeId(currentEmployeeId);
        currentPendingStep.setActedAt(LocalDateTime.now());
        currentPendingStep.setComment(request != null ? request.getComment() : null);
        approvalStepRepository.save(currentPendingStep);

        // Xác định bước tiếp theo
        Map<String, Object> context = (request != null && request.getContextVariables() != null)
                ? request.getContextVariables()
                : Collections.emptyMap();

        WorkflowStep nextStep = resolveNextStep(
                instance.getWorkflowDefinition().getId(),
                currentPendingStep.getStepId(),
                currentPendingStep.getStepOrder(),
                context);

        if (nextStep != null) {
            // Chuyển sang bước kế tiếp
            UUID assignedApproverId = approverResolver.resolveAssignedApprover(
                    nextStep, instance.getRequesterEmployeeId(), instance.getCompanyId());

            LocalDateTime slaDeadline = nextStep.getSlaHours() != null
                    ? LocalDateTime.now().plusHours(nextStep.getSlaHours())
                    : null;

            WorkflowApprovalStep nextApprovalStep = WorkflowApprovalStep.builder()
                    .workflowInstance(instance)
                    .stepId(nextStep.getId())
                    .stepOrder(nextStep.getStepOrder())
                    .stepName(nextStep.getName())
                    .assignedApproverEmployeeId(assignedApproverId)
                    .action(WorkflowAction.PENDING)
                    .slaDeadlineAt(slaDeadline)
                    .build();

            approvalStepRepository.save(nextApprovalStep);
            instance.setCurrentStepId(nextStep.getId());
            instanceRepository.save(instance);

            // Bắn event bước mới
            eventPublisher.publishEvent(WorkflowStepAssignedEvent.builder()
                    .workflowInstanceId(instance.getId())
                    .stepId(nextStep.getId())
                    .stepName(nextStep.getName())
                    .stepOrder(nextStep.getStepOrder())
                    .requestType(instance.getRequestType())
                    .requestId(instance.getRequestId())
                    .requesterEmployeeId(instance.getRequesterEmployeeId())
                    .assignedApproverEmployeeId(assignedApproverId)
                    .slaDeadlineAt(slaDeadline)
                    .build());

            log.info("Workflow instance ID: {} chuyển sang bước tiếp theo: {} (Approver: {})",
                    instance.getId(), nextStep.getName(), assignedApproverId);
        } else {
            // Đã duyệt qua tất cả các bước -> WORKFLOW HOÀN TẤT THÀNH CÔNG
            instance.setStatus(ApprovalStatus.APPROVED);
            instance.setCurrentStepId(null);
            instanceRepository.save(instance);

            eventPublisher.publishEvent(WorkflowCompletedEvent.builder()
                    .workflowInstanceId(instance.getId())
                    .companyId(instance.getCompanyId())
                    .requestType(instance.getRequestType())
                    .requestId(instance.getRequestId())
                    .requesterEmployeeId(instance.getRequesterEmployeeId())
                    .finalStatus(ApprovalStatus.APPROVED)
                    .reason(request != null ? request.getComment() : null)
                    .build());

            log.info("Workflow instance ID: {} ĐÃ ĐƯỢC DUYỆT HOÀN TẤT TOÀN DIỆN", instance.getId());
        }

        return enrichInstanceResponse(instance);
    }

    @Override
    @Transactional
    public WorkflowInstanceResponse reject(UUID instanceId, UUID currentEmployeeId, ApprovalActionRequest request) {
        WorkflowInstance instance = getInstanceOrThrow(instanceId);

        if (instance.getStatus() != ApprovalStatus.PENDING) {
            throw new AppException(WorkflowErrorCode.WORKFLOW_ALREADY_CLOSED);
        }

        WorkflowApprovalStep currentPendingStep = approvalStepRepository
                .findByWorkflowInstanceIdAndAction(instanceId, WorkflowAction.PENDING)
                .orElseThrow(() -> new AppException(WorkflowErrorCode.WORKFLOW_ALREADY_CLOSED));

        // Kiểm tra quyền từ chối
        boolean authorized = approverResolver.isAuthorizedApprover(
                currentPendingStep.getAssignedApproverEmployeeId(), currentEmployeeId, LocalDate.now());
        if (!authorized) {
            throw new AppException(WorkflowErrorCode.NOT_AUTHORIZED_APPROVER);
        }

        currentPendingStep.setAction(WorkflowAction.REJECTED);
        currentPendingStep.setApproverEmployeeId(currentEmployeeId);
        currentPendingStep.setActedAt(LocalDateTime.now());
        currentPendingStep.setComment(request != null ? request.getComment() : null);
        approvalStepRepository.save(currentPendingStep);

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
                .reason(request != null ? request.getComment() : null)
                .build());

        log.info("Workflow instance ID: {} ĐÃ BỊ TỪ CHỐI bởi employee: {}", instance.getId(), currentEmployeeId);
        return enrichInstanceResponse(instance);
    }

    @Override
    @Transactional
    public WorkflowInstanceResponse cancel(UUID instanceId, UUID currentEmployeeId, String reason) {
        WorkflowInstance instance = getInstanceOrThrow(instanceId);

        if (instance.getStatus() != ApprovalStatus.PENDING) {
            throw new AppException(WorkflowErrorCode.WORKFLOW_ALREADY_CLOSED);
        }

        if (!instance.getRequesterEmployeeId().equals(currentEmployeeId)) {
            throw new AppException(WorkflowErrorCode.NOT_AUTHORIZED_APPROVER, "Chỉ người tạo đơn mới có quyền hủy đơn");
        }

        approvalStepRepository.findByWorkflowInstanceIdAndAction(instanceId, WorkflowAction.PENDING)
                .ifPresent(step -> {
                    step.setAction(WorkflowAction.REJECTED);
                    step.setComment("Đã hủy bởi người tạo đơn. Lý do: " + reason);
                    step.setActedAt(LocalDateTime.now());
                    approvalStepRepository.save(step);
                });

        instance.setStatus(ApprovalStatus.CANCELLED);
        instance.setCurrentStepId(null);
        instanceRepository.save(instance);

        eventPublisher.publishEvent(WorkflowCompletedEvent.builder()
                .workflowInstanceId(instance.getId())
                .companyId(instance.getCompanyId())
                .requestType(instance.getRequestType())
                .requestId(instance.getRequestId())
                .requesterEmployeeId(instance.getRequesterEmployeeId())
                .finalStatus(ApprovalStatus.CANCELLED)
                .reason(reason)
                .build());

        log.info("Workflow instance ID: {} ĐÃ BỊ HỦY bởi requester: {}", instance.getId(), currentEmployeeId);
        return enrichInstanceResponse(instance);
    }

    @Override
    @Transactional(readOnly = true)
    public PageData<PendingApprovalResponse> getPendingApprovals(UUID currentEmployeeId, Pageable pageable) {
        // Tìm các delegator ủy quyền cho currentEmployeeId tại thời điểm hiện tại
        List<UUID> delegatorIds = delegateRepository.findDelegatorIdsForDelegate(currentEmployeeId, LocalDate.now());

        Set<UUID> allApproverIds = new HashSet<>(delegatorIds);
        allApproverIds.add(currentEmployeeId);

        Page<WorkflowApprovalStep> stepsPage = approvalStepRepository.findPendingStepsForApprovers(
                allApproverIds, WorkflowAction.PENDING, pageable);

        List<PendingApprovalResponse> responses = stepsPage.getContent().stream()
                .map(step -> {
                    WorkflowInstance inst = step.getWorkflowInstance();
                    boolean isDelegated = !currentEmployeeId.equals(step.getAssignedApproverEmployeeId());

                    PendingApprovalResponse res = PendingApprovalResponse.builder()
                            .workflowInstanceId(inst.getId())
                            .approvalStepId(step.getId())
                            .stepId(step.getStepId())
                            .stepOrder(step.getStepOrder())
                            .stepName(step.getStepName())
                            .requestType(inst.getRequestType())
                            .requestId(inst.getRequestId())
                            .requesterEmployeeId(inst.getRequesterEmployeeId())
                            .assignedApproverEmployeeId(step.getAssignedApproverEmployeeId())
                            .isDelegated(isDelegated)
                            .slaDeadlineAt(step.getSlaDeadlineAt())
                            .requestCreatedAt(inst.getCreatedAt())
                            .build();

                    employeeRepository.findById(inst.getRequesterEmployeeId())
                            .ifPresent(reqEmp -> res.setRequesterEmployeeName(reqEmp.getFullName()));

                    if (isDelegated && step.getAssignedApproverEmployeeId() != null) {
                        res.setDelegatorEmployeeId(step.getAssignedApproverEmployeeId());
                        employeeRepository.findById(step.getAssignedApproverEmployeeId())
                                .ifPresent(delEmp -> res.setDelegatorName(delEmp.getFullName()));
                    }

                    return res;
                })
                .toList();

        return PageData.of(stepsPage, responses);
    }

    @Override
    @Transactional(readOnly = true)
    public WorkflowHistoryResponse getInstanceHistory(UUID instanceId) {
        WorkflowInstance instance = getInstanceOrThrow(instanceId);
        List<WorkflowApprovalStep> steps = approvalStepRepository.findByWorkflowInstanceIdOrderByStepOrderAsc(instanceId);

        WorkflowHistoryResponse history = WorkflowHistoryResponse.builder()
                .instanceId(instance.getId())
                .requestType(instance.getRequestType())
                .requestId(instance.getRequestId())
                .requesterEmployeeId(instance.getRequesterEmployeeId())
                .status(instance.getStatus())
                .workflowDefinitionName(instance.getWorkflowDefinition().getName())
                .workflowVersion(instance.getWorkflowDefinition().getVersion())
                .createdAt(instance.getCreatedAt())
                .build();

        employeeRepository.findById(instance.getRequesterEmployeeId())
                .ifPresent(emp -> history.setRequesterEmployeeName(emp.getFullName()));

        List<WorkflowApprovalStepResponse> stepResponses = steps.stream().map(step -> {
            WorkflowApprovalStepResponse r = workflowMapper.toApprovalStepResponse(step);
            if (step.getAssignedApproverEmployeeId() != null) {
                employeeRepository.findById(step.getAssignedApproverEmployeeId())
                        .ifPresent(e -> r.setAssignedApproverName(e.getFullName()));
            }
            if (step.getApproverEmployeeId() != null) {
                employeeRepository.findById(step.getApproverEmployeeId())
                        .ifPresent(e -> r.setApproverName(e.getFullName()));
            }
            return r;
        }).toList();

        history.setSteps(stepResponses);

        if (instance.getStatus() != ApprovalStatus.PENDING && !steps.isEmpty()) {
            steps.stream()
                    .filter(s -> s.getActedAt() != null)
                    .max(Comparator.comparing(WorkflowApprovalStep::getActedAt))
                    .ifPresent(lastStep -> history.setCompletedAt(lastStep.getActedAt()));
        }

        return history;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<WorkflowInstanceResponse> getInstanceByRequest(ApprovalRequestType requestType, UUID requestId) {
        return instanceRepository.findByRequestTypeAndRequestId(requestType, requestId)
                .map(this::enrichInstanceResponse);
    }

    private WorkflowStep resolveFirstStep(UUID definitionId, List<WorkflowStep> allSteps, Map<String, Object> context) {
        List<WorkflowCondition> entryConditions = conditionRepository
                .findByWorkflowDefinitionIdAndFromStepIdIsNullOrderByPriorityAsc(definitionId);

        for (WorkflowCondition cond : entryConditions) {
            if (conditionEvaluator.evaluate(cond.getConditionExpression(), context)) {
                return stepRepository.findById(cond.getToStepId())
                        .orElseThrow(() -> new AppException(WorkflowErrorCode.WORKFLOW_STEP_NOT_FOUND));
            }
        }

        // Mặc định là bước có stepOrder nhỏ nhất
        return allSteps.get(0);
    }

    private WorkflowStep resolveNextStep(
            UUID definitionId, UUID currentStepId, Integer currentStepOrder, Map<String, Object> context) {

        // 1. Kiểm tra điều kiện rẽ nhánh xuất phát từ bước hiện tại
        List<WorkflowCondition> conditions = conditionRepository
                .findByWorkflowDefinitionIdAndFromStepIdOrderByPriorityAsc(definitionId, currentStepId);

        for (WorkflowCondition cond : conditions) {
            if (conditionEvaluator.evaluate(cond.getConditionExpression(), context)) {
                return stepRepository.findById(cond.getToStepId())
                        .orElseThrow(() -> new AppException(WorkflowErrorCode.WORKFLOW_STEP_NOT_FOUND));
            }
        }

        // 2. Không có điều kiện rẽ nhánh thỏa mãn -> Tìm bước tiếp theo theo thứ tự tăng dần
        List<WorkflowStep> steps = stepRepository.findByWorkflowDefinitionIdOrderByStepOrderAsc(definitionId);
        return steps.stream()
                .filter(s -> s.getStepOrder() > currentStepOrder)
                .findFirst()
                .orElse(null); // null = hết các bước
    }

    private WorkflowInstance getInstanceOrThrow(UUID id) {
        return instanceRepository.findById(id)
                .orElseThrow(() -> new AppException(WorkflowErrorCode.WORKFLOW_INSTANCE_NOT_FOUND));
    }

    private WorkflowInstanceResponse enrichInstanceResponse(WorkflowInstance instance) {
        WorkflowInstanceResponse response = workflowMapper.toInstanceResponse(instance);

        employeeRepository.findById(instance.getRequesterEmployeeId())
                .ifPresent(emp -> response.setRequesterEmployeeName(emp.getFullName()));

        if (instance.getCurrentStepId() != null) {
            stepRepository.findById(instance.getCurrentStepId())
                    .ifPresent(s -> response.setCurrentStepName(s.getName()));
        }

        List<WorkflowApprovalStep> steps = approvalStepRepository
                .findByWorkflowInstanceIdOrderByStepOrderAsc(instance.getId());

        List<WorkflowApprovalStepResponse> stepResponses = steps.stream().map(step -> {
            WorkflowApprovalStepResponse r = workflowMapper.toApprovalStepResponse(step);
            if (step.getAssignedApproverEmployeeId() != null) {
                employeeRepository.findById(step.getAssignedApproverEmployeeId())
                        .ifPresent(e -> r.setAssignedApproverName(e.getFullName()));
            }
            if (step.getApproverEmployeeId() != null) {
                employeeRepository.findById(step.getApproverEmployeeId())
                        .ifPresent(e -> r.setApproverName(e.getFullName()));
            }
            return r;
        }).toList();

        response.setApprovalSteps(stepResponses);
        return response;
    }
}
