package com.cyclosa.workflow;

import com.cyclosa.auth.entity.User;
import com.cyclosa.auth.repository.UserRepository;
import com.cyclosa.common.exception.AppException;
import com.cyclosa.employee.entity.Employee;
import com.cyclosa.employee.repository.EmployeeRepository;
import com.cyclosa.workflow.dto.request.ApproveStepRequest;
import com.cyclosa.workflow.dto.request.RejectStepRequest;
import com.cyclosa.workflow.dto.request.SubmitWorkflowRequest;
import com.cyclosa.workflow.dto.response.WorkflowRequestDetailResponse;
import com.cyclosa.workflow.entity.WorkflowDefinition;
import com.cyclosa.workflow.entity.WorkflowDefinitionStep;
import com.cyclosa.workflow.entity.WorkflowRequest;
import com.cyclosa.workflow.entity.WorkflowRequestStep;
import com.cyclosa.workflow.enums.ApproverType;
import com.cyclosa.workflow.enums.RequestType;
import com.cyclosa.workflow.enums.StepConditionType;
import com.cyclosa.workflow.enums.StepDecision;
import com.cyclosa.workflow.enums.WorkflowOverallStatus;
import com.cyclosa.workflow.event.WorkflowCompletedEvent;
import com.cyclosa.workflow.repository.WorkflowDefinitionRepository;
import com.cyclosa.workflow.repository.WorkflowRequestRepository;
import com.cyclosa.workflow.repository.WorkflowRequestStepRepository;
import com.cyclosa.workflow.service.ApproverResolverService;
import com.cyclosa.workflow.service.WorkflowExecutionService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkflowServiceTest {

    @Mock
    private WorkflowDefinitionRepository workflowDefinitionRepository;

    @Mock
    private WorkflowRequestRepository workflowRequestRepository;

    @Mock
    private WorkflowRequestStepRepository workflowRequestStepRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApproverResolverService approverResolverService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private WorkflowExecutionService workflowExecutionService;

    private UUID userId;
    private UUID employeeId;
    private UUID companyId;
    private UUID referenceId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        employeeId = UUID.randomUUID();
        companyId = UUID.randomUUID();
        referenceId = UUID.randomUUID();

        // Set real authentication details in SecurityContextHolder
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());
        auth.setDetails(userId.toString());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Submit: Tự động duyệt khi không có quy trình định nghĩa cho công ty")
    void submit_autoApprove_whenNoDefinition() {
        when(workflowDefinitionRepository.findByCompanyIdAndRequestTypeAndIsActiveTrue(companyId, RequestType.CONTRACT))
                .thenReturn(Optional.empty());

        SubmitWorkflowRequest req = SubmitWorkflowRequest.builder()
                .companyId(companyId)
                .requestType(RequestType.CONTRACT)
                .referenceId(referenceId)
                .referenceCode("HD-001")
                .title("Phê duyệt hợp đồng")
                .build();

        WorkflowRequestDetailResponse response = workflowExecutionService.submit(req);

        assertThat(response.getOverallStatus()).isEqualTo(WorkflowOverallStatus.APPROVED);
        verify(eventPublisher).publishEvent(any(WorkflowCompletedEvent.class));
    }

    @Test
    @DisplayName("Submit: Khởi tạo quy trình 2 bước thành công")
    void submit_withSteps_success() {
        UUID approverId = UUID.randomUUID();

        WorkflowDefinitionStep step1 = WorkflowDefinitionStep.builder()
                .stepOrder(1)
                .stepName("Quản lý duyệt")
                .approverType(ApproverType.DIRECT_MANAGER)
                .conditionType(StepConditionType.ALWAYS)
                .build();
        step1.setId(UUID.randomUUID());

        WorkflowDefinitionStep step2 = WorkflowDefinitionStep.builder()
                .stepOrder(2)
                .stepName("Lương cao duyệt")
                .approverType(ApproverType.ROLE)
                .conditionType(StepConditionType.AMOUNT_ABOVE_THRESHOLD)
                .conditionThreshold(new BigDecimal("50000000"))
                .build();
        step2.setId(UUID.randomUUID());

        WorkflowDefinition def = WorkflowDefinition.builder()
                .companyId(companyId)
                .requestType(RequestType.CONTRACT)
                .name("Quy trình duyệt HĐLĐ")
                .isActive(true)
                .steps(List.of(step1, step2))
                .build();
        def.setId(UUID.randomUUID());

        when(workflowDefinitionRepository.findByCompanyIdAndRequestTypeAndIsActiveTrue(companyId, RequestType.CONTRACT))
                .thenReturn(Optional.of(def));
        when(employeeRepository.findByUserId(userId))
                .thenReturn(Optional.of(Employee.builder().firstName("Van").lastName("Nguyen").build()));
        when(approverResolverService.resolveApprover(any(), any(), any(), any(), any(), any()))
                .thenReturn(approverId);
        when(workflowRequestRepository.save(any(WorkflowRequest.class)))
                .thenAnswer(invocation -> {
                    WorkflowRequest saved = invocation.getArgument(0);
                    if (saved.getId() == null) saved.setId(UUID.randomUUID());
                    return saved;
                });
        when(workflowRequestRepository.findById(any())).thenAnswer(inv -> {
            WorkflowRequest r = WorkflowRequest.builder()
                    .workflowDefinition(def)
                    .requestType(RequestType.CONTRACT)
                    .referenceId(referenceId)
                    .title("Hợp đồng")
                    .requestedByUserId(userId)
                    .steps(new ArrayList<>())
                    .build();
            r.setId(inv.getArgument(0));
            return Optional.of(r);
        });

        SubmitWorkflowRequest req = SubmitWorkflowRequest.builder()
                .companyId(companyId)
                .requestType(RequestType.CONTRACT)
                .referenceId(referenceId)
                .referenceCode("HD-001")
                .title("Phê duyệt hợp đồng")
                .requestAmount(new BigDecimal("20000000"))
                .build();

        WorkflowRequestDetailResponse response = workflowExecutionService.submit(req);

        assertThat(response).isNotNull();
        verify(workflowRequestRepository, atLeastOnce()).save(any(WorkflowRequest.class));
    }

    @Test
    @DisplayName("ApproveStep: Duyệt bước chuyển tiếp thành công sang bước sau")
    void approveStep_advancesNextStep() {
        UUID reqId = UUID.randomUUID();
        UUID step1Id = UUID.randomUUID();
        UUID step2Id = UUID.randomUUID();

        WorkflowRequestStep step1 = WorkflowRequestStep.builder()
                .stepOrder(1)
                .stepName("Bước 1")
                .assigneeEmployeeId(employeeId)
                .isCurrent(true)
                .build();
        step1.setId(step1Id);

        WorkflowRequestStep step2 = WorkflowRequestStep.builder()
                .stepOrder(2)
                .stepName("Bước 2")
                .assigneeEmployeeId(UUID.randomUUID())
                .isCurrent(false)
                .build();
        step2.setId(step2Id);

        WorkflowRequest req = WorkflowRequest.builder()
                .requestType(RequestType.CONTRACT)
                .referenceId(referenceId)
                .title("Phê duyệt hợp đồng")
                .requestedByUserId(userId)
                .steps(new ArrayList<>(List.of(step1, step2)))
                .build();
        req.setId(reqId);
        step1.setWorkflowRequest(req);
        step2.setWorkflowRequest(req);

        when(workflowRequestRepository.findById(reqId)).thenReturn(Optional.of(req));
        when(employeeRepository.findByUserId(userId))
                .thenReturn(Optional.of(Employee.builder().firstName("A").lastName("B").build()));

        ApproveStepRequest approveReq = ApproveStepRequest.builder().comment("Đồng ý").build();
        WorkflowRequestDetailResponse res = workflowExecutionService.approveStep(reqId, step1Id, approveReq);

        assertThat(step1.getDecision()).isEqualTo(StepDecision.APPROVED);
        assertThat(step1.getIsCurrent()).isFalse();
        assertThat(step2.getIsCurrent()).isTrue();
    }

    @Test
    @DisplayName("RejectStep: Từ chối bước bắt buộc có nhận xét tối thiểu 5 ký tự")
    void rejectStep_requiresComment() {
        UUID reqId = UUID.randomUUID();
        UUID stepId = UUID.randomUUID();

        WorkflowRequestStep step = WorkflowRequestStep.builder()
                .stepOrder(1)
                .stepName("Bước 1")
                .assigneeEmployeeId(employeeId)
                .isCurrent(true)
                .build();
        step.setId(stepId);

        WorkflowRequest req = WorkflowRequest.builder()
                .requestType(RequestType.CONTRACT)
                .referenceId(referenceId)
                .title("Hợp đồng")
                .requestedByUserId(userId)
                .steps(new ArrayList<>(List.of(step)))
                .build();
        req.setId(reqId);

        when(workflowRequestRepository.findById(reqId)).thenReturn(Optional.of(req));

        RejectStepRequest shortComment = RejectStepRequest.builder().comment("Ko").build();
        assertThatThrownBy(() -> workflowExecutionService.rejectStep(reqId, stepId, shortComment))
                .isInstanceOf(AppException.class);
    }
}

