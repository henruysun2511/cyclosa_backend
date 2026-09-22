package com.cyclosa.payroll;

import com.cyclosa.common.dto.summary.CompanySummary;
import com.cyclosa.common.dto.summary.EmployeeSummary;
import com.cyclosa.common.exception.AppException;
import com.cyclosa.common.security.SecurityPermissionEvaluator;
import com.cyclosa.contract.dto.response.ContractResponse;
import com.cyclosa.contract.service.ContractService;
import com.cyclosa.employee.dto.response.EmployeeDetailResponse;
import com.cyclosa.employee.service.EmployeeService;
import com.cyclosa.organization.service.CompanyService;
import com.cyclosa.payroll.dto.request.CreateSalaryAdvanceRequest;
import com.cyclosa.payroll.dto.response.SalaryAdvanceResponse;
import com.cyclosa.payroll.entity.SalaryAdvance;
import com.cyclosa.payroll.enums.SalaryAdvanceStatus;
import com.cyclosa.payroll.mapper.SalaryAdvanceMapper;
import com.cyclosa.payroll.repository.SalaryAdvanceRepository;
import com.cyclosa.payroll.service.SalaryAdvanceService;
import com.cyclosa.workflow.dto.request.StartWorkflowRequest;
import com.cyclosa.workflow.dto.response.WorkflowInstanceResponse;
import com.cyclosa.workflow.service.WorkflowEngineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SalaryAdvanceServiceTest {

    @Mock
    private SalaryAdvanceRepository repository;
    @Mock
    private EmployeeService employeeService;
    @Mock
    private ContractService contractService;
    @Mock
    private CompanyService companyService;
    @Mock
    private WorkflowEngineService workflowEngineService;
    @Mock
    private SecurityPermissionEvaluator securityPermissionEvaluator;

    private SalaryAdvanceMapper mapper = Mappers.getMapper(SalaryAdvanceMapper.class);

    private SalaryAdvanceService service;

    private UUID currentUserId;
    private UUID companyId;
    private UUID employeeId;

    @BeforeEach
    void setUp() {
        service = new SalaryAdvanceService(
                repository, employeeService, contractService, companyService,
                workflowEngineService, securityPermissionEvaluator, mapper
        );
        currentUserId = UUID.randomUUID();
        companyId = UUID.randomUUID();
        employeeId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Should create salary advance successfully when within 50% basic salary limit")
    void testCreateSalaryAdvanceSuccess() {
        CreateSalaryAdvanceRequest req = CreateSalaryAdvanceRequest.builder()
                .requestDate(LocalDate.now())
                .amount(new BigDecimal("5000000"))
                .reason("Tạm ứng chi phí cá nhân")
                .build();

        when(employeeService.findEmployeeIdByUserId(currentUserId)).thenReturn(Optional.of(employeeId));

        EmployeeDetailResponse empDetail = EmployeeDetailResponse.builder()
                .id(employeeId)
                .company(CompanySummary.builder().id(companyId).build())
                .build();
        when(employeeService.getEmployeeByIdInternal(employeeId)).thenReturn(empDetail);

        ContractResponse contract = ContractResponse.builder()
                .basicSalary(new BigDecimal("15000000"))
                .build();
        when(contractService.getActiveContractByEmployee(employeeId)).thenReturn(Optional.of(contract));

        when(repository.save(any(SalaryAdvance.class))).thenAnswer(inv -> {
            SalaryAdvance sa = inv.getArgument(0);
            ReflectionTestUtils.setField(sa, "id", UUID.randomUUID());
            return sa;
        });

        WorkflowInstanceResponse wfInstance = WorkflowInstanceResponse.builder()
                .id(UUID.randomUUID())
                .build();
        when(workflowEngineService.startWorkflow(any(StartWorkflowRequest.class))).thenReturn(wfInstance);

        EmployeeSummary empSummary = EmployeeSummary.builder().id(employeeId).fullName("Nguyễn Văn A").build();
        when(employeeService.getEmployeeSummary(employeeId)).thenReturn(empSummary);

        SalaryAdvanceResponse resp = service.createAdvance(currentUserId, req);

        assertThat(resp).isNotNull();
        assertThat(resp.getAmount()).isEqualByComparingTo(new BigDecimal("5000000"));
        assertThat(resp.getStatus()).isEqualTo(SalaryAdvanceStatus.PENDING_APPROVAL);
        verify(repository, atLeastOnce()).save(any(SalaryAdvance.class));
        verify(workflowEngineService).startWorkflow(any(StartWorkflowRequest.class));
    }

    @Test
    @DisplayName("Should throw exception when advance amount exceeds 50% basic salary limit")
    void testCreateSalaryAdvanceExceedsLimit() {
        CreateSalaryAdvanceRequest req = CreateSalaryAdvanceRequest.builder()
                .requestDate(LocalDate.now())
                .amount(new BigDecimal("10000000")) // 10M > 50% of 15M (7.5M)
                .reason("Tạm ứng vượt mức")
                .build();

        when(employeeService.findEmployeeIdByUserId(currentUserId)).thenReturn(Optional.of(employeeId));

        EmployeeDetailResponse empDetail = EmployeeDetailResponse.builder()
                .id(employeeId)
                .company(CompanySummary.builder().id(companyId).build())
                .build();
        when(employeeService.getEmployeeByIdInternal(employeeId)).thenReturn(empDetail);

        ContractResponse contract = ContractResponse.builder()
                .basicSalary(new BigDecimal("15000000"))
                .build();
        when(contractService.getActiveContractByEmployee(employeeId)).thenReturn(Optional.of(contract));

        assertThatThrownBy(() -> service.createAdvance(currentUserId, req))
                .isInstanceOf(AppException.class);
    }
}
