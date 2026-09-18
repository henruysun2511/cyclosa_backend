package com.cyclosa.workflow;

import com.cyclosa.common.exception.AppException;
import com.cyclosa.employee.repository.EmployeeRepository;
import com.cyclosa.workflow.dto.request.CreateWorkflowDelegateRequest;
import com.cyclosa.workflow.dto.response.WorkflowDelegateResponse;
import com.cyclosa.workflow.entity.WorkflowDelegate;
import com.cyclosa.workflow.exception.WorkflowErrorCode;
import com.cyclosa.workflow.mapper.WorkflowMapper;
import com.cyclosa.workflow.repository.WorkflowDelegateRepository;
import com.cyclosa.workflow.service.WorkflowDelegateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WorkflowDelegateServiceTest {

    @Mock
    private WorkflowDelegateRepository delegateRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private WorkflowMapper workflowMapper;

    @InjectMocks
    private WorkflowDelegateService delegateService;

    private UUID delegatorId;
    private UUID delegateId;
    private UUID companyId;

    @BeforeEach
    void setUp() {
        delegatorId = UUID.randomUUID();
        delegateId = UUID.randomUUID();
        companyId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Không được tự ủy quyền cho chính mình -> ném CANNOT_DELEGATE_TO_SELF")
    void testCannotDelegateToSelf() {
        CreateWorkflowDelegateRequest request = CreateWorkflowDelegateRequest.builder()
                .companyId(companyId)
                .delegateEmployeeId(delegatorId) // Cùng ID
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(5))
                .build();

        assertThatThrownBy(() -> delegateService.createDelegate(request, delegatorId))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Không thể ủy quyền");
    }

    @Test
    @DisplayName("Ngày kết thúc trước ngày bắt đầu -> ném INVALID_DELEGATION_DATE")
    void testInvalidDates() {
        CreateWorkflowDelegateRequest request = CreateWorkflowDelegateRequest.builder()
                .companyId(companyId)
                .delegateEmployeeId(delegateId)
                .startDate(LocalDate.now().plusDays(5))
                .endDate(LocalDate.now().plusDays(2))
                .build();

        assertThatThrownBy(() -> delegateService.createDelegate(request, delegatorId))
                .isInstanceOf(AppException.class);
    }

    @Test
    @DisplayName("Khoảng thời gian bị trùng lặp với ủy quyền đang có hiệu lực -> ném OVERLAPPING_DELEGATION")
    void testOverlappingDelegation() {
        CreateWorkflowDelegateRequest request = CreateWorkflowDelegateRequest.builder()
                .companyId(companyId)
                .delegateEmployeeId(delegateId)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(5))
                .build();

        given(delegateRepository.existsOverlapping(delegatorId, request.getStartDate(), request.getEndDate(), null))
                .willReturn(true);

        assertThatThrownBy(() -> delegateService.createDelegate(request, delegatorId))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("trùng lặp");
    }

    @Test
    @DisplayName("Tạo ủy quyền thành công khi thông tin hợp lệ")
    void testCreateDelegateSuccess() {
        CreateWorkflowDelegateRequest request = CreateWorkflowDelegateRequest.builder()
                .companyId(companyId)
                .delegateEmployeeId(delegateId)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(5))
                .reason("Đi công tác nước ngoài")
                .build();

        WorkflowDelegate delegateEntity = WorkflowDelegate.builder()
                .companyId(companyId)
                .delegatorEmployeeId(delegatorId)
                .delegateEmployeeId(delegateId)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .reason(request.getReason())
                .isActive(true)
                .build();

        WorkflowDelegateResponse expectedResponse = WorkflowDelegateResponse.builder()
                .id(UUID.randomUUID())
                .delegatorEmployeeId(delegatorId)
                .delegateEmployeeId(delegateId)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .isActive(true)
                .build();

        given(delegateRepository.existsOverlapping(delegatorId, request.getStartDate(), request.getEndDate(), null))
                .willReturn(false);
        given(workflowMapper.toDelegateEntity(request)).willReturn(delegateEntity);
        given(delegateRepository.save(any(WorkflowDelegate.class))).willReturn(delegateEntity);
        given(workflowMapper.toDelegateResponse(delegateEntity)).willReturn(expectedResponse);

        WorkflowDelegateResponse actual = delegateService.createDelegate(request, delegatorId);

        assertThat(actual).isNotNull();
        assertThat(actual.getDelegatorEmployeeId()).isEqualTo(delegatorId);
        verify(delegateRepository).save(any(WorkflowDelegate.class));
    }
}
