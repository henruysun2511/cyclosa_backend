package com.cyclosa.payroll;

import com.cyclosa.attendance.service.TimesheetService;
import com.cyclosa.common.exception.AppException;
import com.cyclosa.contract.service.ContractService;
import com.cyclosa.employee.service.EmployeeService;
import com.cyclosa.organization.service.CompanyService;
import com.cyclosa.payroll.dto.request.CreatePayrollPeriodRequest;
import com.cyclosa.payroll.dto.response.PayrollPeriodResponse;
import com.cyclosa.payroll.entity.PayrollPeriod;
import com.cyclosa.payroll.enums.PayrollPeriodStatus;
import com.cyclosa.payroll.mapper.PayrollPeriodMapper;
import com.cyclosa.payroll.repository.PayrollPeriodRepository;
import com.cyclosa.payroll.repository.PayrollRecordItemRepository;
import com.cyclosa.payroll.repository.PayrollRecordRepository;
import com.cyclosa.payroll.repository.SalaryAdvanceRepository;
import com.cyclosa.payroll.repository.SalaryComponentRepository;
import com.cyclosa.payroll.service.PayrollCalculationEngine;
import com.cyclosa.payroll.service.PayrollPeriodService;
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
class PayrollPeriodServiceTest {

    @Mock
    private PayrollPeriodRepository periodRepository;
    @Mock
    private PayrollRecordRepository recordRepository;
    @Mock
    private PayrollRecordItemRepository itemRepository;
    @Mock
    private SalaryAdvanceRepository advanceRepository;
    @Mock
    private SalaryComponentRepository componentRepository;
    @Mock
    private PayrollCalculationEngine calculationEngine;
    @Mock
    private TimesheetService timesheetService;
    @Mock
    private ContractService contractService;
    @Mock
    private EmployeeService employeeService;
    @Mock
    private CompanyService companyService;
    @Mock
    private WorkflowEngineService workflowEngineService;

    private PayrollPeriodMapper periodMapper = Mappers.getMapper(PayrollPeriodMapper.class);

    private PayrollPeriodService service;

    private UUID companyId;

    @BeforeEach
    void setUp() {
        service = new PayrollPeriodService(
                periodRepository, recordRepository, itemRepository, advanceRepository,
                componentRepository, calculationEngine, timesheetService, contractService,
                employeeService, companyService, workflowEngineService, periodMapper
        );
        companyId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Should create payroll period successfully")
    void testCreatePayrollPeriodSuccess() {
        CreatePayrollPeriodRequest req = CreatePayrollPeriodRequest.builder()
                .name("Kỳ lương tháng 10/2026")
                .code("PR-2026-10")
                .month(10)
                .year(2026)
                .startDate(LocalDate.of(2026, 10, 1))
                .endDate(LocalDate.of(2026, 10, 31))
                .payDate(LocalDate.of(2026, 11, 5))
                .standardWorkDays(BigDecimal.valueOf(22))
                .build();

        when(periodRepository.existsByCodeAndCompanyId("PR-2026-10", companyId)).thenReturn(false);
        when(periodRepository.existsByCompanyIdAndMonthAndYear(companyId, 10, 2026)).thenReturn(false);
        when(periodRepository.save(any(PayrollPeriod.class))).thenAnswer(inv -> {
            PayrollPeriod p = inv.getArgument(0);
            ReflectionTestUtils.setField(p, "id", UUID.randomUUID());
            return p;
        });

        PayrollPeriodResponse resp = service.createPeriod(companyId, req);

        assertThat(resp).isNotNull();
        assertThat(resp.getName()).isEqualTo("Kỳ lương tháng 10/2026");
        assertThat(resp.getStatus()).isEqualTo(PayrollPeriodStatus.OPEN);
        verify(periodRepository).save(any(PayrollPeriod.class));
    }

    @Test
    @DisplayName("Should throw exception when creating duplicate period for same month and year")
    void testCreateDuplicatePayrollPeriod() {
        CreatePayrollPeriodRequest req = CreatePayrollPeriodRequest.builder()
                .name("Kỳ lương tháng 10/2026")
                .code("PR-2026-10")
                .month(10)
                .year(2026)
                .build();

        when(periodRepository.existsByCodeAndCompanyId("PR-2026-10", companyId)).thenReturn(false);
        when(periodRepository.existsByCompanyIdAndMonthAndYear(companyId, 10, 2026)).thenReturn(true);

        assertThatThrownBy(() -> service.createPeriod(companyId, req))
                .isInstanceOf(AppException.class);
    }

    @Test
    @DisplayName("Should submit period for approval successfully")
    void testSubmitApprovalSuccess() {
        UUID periodId = UUID.randomUUID();
        PayrollPeriod period = PayrollPeriod.builder()
                .companyId(companyId)
                .code("PR-2026-10")
                .month(10)
                .year(2026)
                .totalGross(new BigDecimal("50000000"))
                .totalNet(new BigDecimal("42000000"))
                .status(PayrollPeriodStatus.PROCESSING)
                .build();
        ReflectionTestUtils.setField(period, "id", periodId);

        when(periodRepository.findByIdAndCompanyId(periodId, companyId)).thenReturn(Optional.of(period));
        when(workflowEngineService.startWorkflow(any(StartWorkflowRequest.class))).thenReturn(
                WorkflowInstanceResponse.builder().id(UUID.randomUUID()).build()
        );
        when(periodRepository.save(any(PayrollPeriod.class))).thenAnswer(inv -> inv.getArgument(0));

        PayrollPeriodResponse resp = service.submitApproval(companyId, periodId);

        assertThat(resp).isNotNull();
        verify(workflowEngineService).startWorkflow(any(StartWorkflowRequest.class));
        verify(periodRepository).save(period);
    }
}
