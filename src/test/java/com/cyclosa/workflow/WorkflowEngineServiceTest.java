package com.cyclosa.workflow;

import com.cyclosa.common.exception.AppException;
import com.cyclosa.employee.repository.EmployeeRepository;
import com.cyclosa.workflow.dto.request.ApprovalActionRequest;
import com.cyclosa.workflow.dto.request.StartWorkflowRequest;
import com.cyclosa.workflow.dto.response.WorkflowInstanceResponse;
import com.cyclosa.workflow.entity.*;
import com.cyclosa.workflow.enums.*;
import com.cyclosa.workflow.mapper.WorkflowMapper;
import com.cyclosa.workflow.repository.*;
import com.cyclosa.workflow.service.ApproverResolverService;
import com.cyclosa.workflow.service.ConditionEvaluatorService;
import com.cyclosa.workflow.service.WorkflowEngineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WorkflowEngineServiceTest {

    @Mock private WorkflowDefinitionRepository definitionRepository;
    @Mock private WorkflowStepRepository stepRepository;
    @Mock private WorkflowConditionRepository conditionRepository;
    @Mock private WorkflowInstanceRepository instanceRepository;
    @Mock private WorkflowApprovalStepRepository approvalStepRepository;
    @Mock private WorkflowDelegateRepository delegateRepository;
    @Mock private EmployeeRepository employeeRepository;
    @Mock private ApproverResolverService approverResolver;
    @Mock private ConditionEvaluatorService conditionEvaluator;
    @Mock private WorkflowMapper workflowMapper;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private WorkflowEngineService workflowEngine;

    private UUID companyId;
    private UUID requesterId;
    private UUID approverId;
    private UUID requestId;
    private WorkflowDefinition definition;
    private WorkflowStep step1;
    private WorkflowStep step2;

    @BeforeEach
    void setUp() {
        companyId = UUID.randomUUID();
        requesterId = UUID.randomUUID();
        approverId = UUID.randomUUID();
        requestId = UUID.randomUUID();

        definition = WorkflowDefinition.builder()
                .companyId(companyId)
                .requestType(ApprovalRequestType.LEAVE_REQUEST)
                .name("Quy trình duyệt nghỉ phép")
                .version(1)
                .status(WorkflowStatus.PUBLISHED)
                .build();
        definition.setId(UUID.randomUUID());

        step1 = WorkflowStep.builder()
                .workflowDefinition(definition)
                .stepOrder(1)
                .name("Quản lý trực tiếp duyệt")
                .approverType(ApproverType.DIRECT_MANAGER)
                .slaHours(24)
                .build();
        step1.setId(UUID.randomUUID());

        step2 = WorkflowStep.builder()
                .workflowDefinition(definition)
                .stepOrder(2)
                .name("HR duyệt")
                .approverType(ApproverType.SPECIFIC_ROLE)
                .slaHours(48)
                .build();
        step2.setId(UUID.randomUUID());
    }

    @Test
    @DisplayName("Khởi tạo workflow thành công: gán bước 1 và tạo approval step PENDING")
    void testStartWorkflowSuccess() {
        StartWorkflowRequest request = StartWorkflowRequest.builder()
                .companyId(companyId)
                .requestType(ApprovalRequestType.LEAVE_REQUEST)
                .requestId(requestId)
                .requesterEmployeeId(requesterId)
                .build();

        given(definitionRepository.findByCompanyIdAndRequestTypeAndStatus(
                companyId, ApprovalRequestType.LEAVE_REQUEST, WorkflowStatus.PUBLISHED))
                .willReturn(Optional.of(definition));
        given(instanceRepository.findByRequestTypeAndRequestId(ApprovalRequestType.LEAVE_REQUEST, requestId))
                .willReturn(Optional.empty());
        given(stepRepository.findByWorkflowDefinitionIdOrderByStepOrderAsc(definition.getId()))
                .willReturn(List.of(step1, step2));
        given(conditionRepository.findByWorkflowDefinitionIdAndFromStepIdIsNullOrderByPriorityAsc(definition.getId()))
                .willReturn(Collections.emptyList());
        given(approverResolver.resolveAssignedApprover(step1, requesterId, companyId))
                .willReturn(approverId);

        WorkflowInstance savedInstance = WorkflowInstance.builder()
                .workflowDefinition(definition)
                .companyId(companyId)
                .requestType(ApprovalRequestType.LEAVE_REQUEST)
                .requestId(requestId)
                .requesterEmployeeId(requesterId)
                .status(ApprovalStatus.PENDING)
                .currentStepId(step1.getId())
                .approvalSteps(new ArrayList<>())
                .build();
        savedInstance.setId(UUID.randomUUID());

        WorkflowApprovalStep approvalStep = WorkflowApprovalStep.builder()
                .workflowInstance(savedInstance)
                .stepId(step1.getId())
                .stepOrder(step1.getStepOrder())
                .stepName(step1.getName())
                .assignedApproverEmployeeId(approverId)
                .action(WorkflowAction.PENDING)
                .build();
        approvalStep.setId(UUID.randomUUID());

        given(instanceRepository.save(any(WorkflowInstance.class))).willReturn(savedInstance);
        given(approvalStepRepository.save(any(WorkflowApprovalStep.class))).willReturn(approvalStep);
        given(workflowMapper.toInstanceResponse(savedInstance)).willReturn(
                WorkflowInstanceResponse.builder().id(savedInstance.getId()).status(ApprovalStatus.PENDING).build());

        WorkflowInstanceResponse response = workflowEngine.startWorkflow(request);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(ApprovalStatus.PENDING);
        verify(instanceRepository).save(any(WorkflowInstance.class));
        verify(approvalStepRepository).save(any(WorkflowApprovalStep.class));
    }

    @Test
    @DisplayName("Duyệt bước: Người không có thẩm quyền -> ném NOT_AUTHORIZED_APPROVER")
    void testApproveUnauthorized() {
        UUID instanceId = UUID.randomUUID();
        UUID unauthorizedUserId = UUID.randomUUID();

        WorkflowInstance instance = WorkflowInstance.builder()
                .workflowDefinition(definition)
                .companyId(companyId)
                .requestType(ApprovalRequestType.LEAVE_REQUEST)
                .requestId(requestId)
                .requesterEmployeeId(requesterId)
                .status(ApprovalStatus.PENDING)
                .currentStepId(step1.getId())
                .build();

        WorkflowApprovalStep pendingStep = WorkflowApprovalStep.builder()
                .workflowInstance(instance)
                .stepId(step1.getId())
                .stepOrder(1)
                .assignedApproverEmployeeId(approverId)
                .action(WorkflowAction.PENDING)
                .build();

        given(instanceRepository.findById(instanceId)).willReturn(Optional.of(instance));
        given(approvalStepRepository.findByWorkflowInstanceIdAndAction(instanceId, WorkflowAction.PENDING))
                .willReturn(Optional.of(pendingStep));
        given(approverResolver.isAuthorizedApprover(eq(approverId), eq(unauthorizedUserId), any(LocalDate.class)))
                .willReturn(false);

        assertThatThrownBy(() -> workflowEngine.approve(instanceId, unauthorizedUserId, new ApprovalActionRequest()))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("không có quyền");
    }

    @Test
    @DisplayName("Từ chối bước: Instance chuyển sang REJECTED và hoàn tất")
    void testRejectSuccess() {
        UUID instanceId = UUID.randomUUID();

        WorkflowInstance instance = WorkflowInstance.builder()
                .workflowDefinition(definition)
                .companyId(companyId)
                .requestType(ApprovalRequestType.LEAVE_REQUEST)
                .requestId(requestId)
                .requesterEmployeeId(requesterId)
                .status(ApprovalStatus.PENDING)
                .currentStepId(step1.getId())
                .build();

        WorkflowApprovalStep pendingStep = WorkflowApprovalStep.builder()
                .workflowInstance(instance)
                .stepId(step1.getId())
                .stepOrder(1)
                .assignedApproverEmployeeId(approverId)
                .action(WorkflowAction.PENDING)
                .build();

        given(instanceRepository.findById(instanceId)).willReturn(Optional.of(instance));
        given(approvalStepRepository.findByWorkflowInstanceIdAndAction(instanceId, WorkflowAction.PENDING))
                .willReturn(Optional.of(pendingStep));
        given(approverResolver.isAuthorizedApprover(eq(approverId), eq(approverId), any(LocalDate.class)))
                .willReturn(true);
        given(workflowMapper.toInstanceResponse(instance)).willReturn(
                WorkflowInstanceResponse.builder().id(instanceId).status(ApprovalStatus.REJECTED).build());

        ApprovalActionRequest rejectReq = ApprovalActionRequest.builder().comment("Không đủ nhân sự trực").build();
        WorkflowInstanceResponse response = workflowEngine.reject(instanceId, approverId, rejectReq);

        assertThat(response.getStatus()).isEqualTo(ApprovalStatus.REJECTED);
        assertThat(instance.getStatus()).isEqualTo(ApprovalStatus.REJECTED);
        assertThat(pendingStep.getAction()).isEqualTo(WorkflowAction.REJECTED);
        assertThat(pendingStep.getComment()).isEqualTo("Không đủ nhân sự trực");
    }
}
