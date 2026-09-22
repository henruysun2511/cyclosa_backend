package com.cyclosa.payroll;

import com.cyclosa.common.dto.summary.EmployeeSummary;
import com.cyclosa.common.exception.AppException;
import com.cyclosa.common.security.SecurityPermissionEvaluator;
import com.cyclosa.employee.service.EmployeeService;
import com.cyclosa.payroll.dto.request.AdjustPayrollRecordRequest;
import com.cyclosa.payroll.dto.response.PayrollRecordDetailResponse;
import com.cyclosa.payroll.entity.PayrollPeriod;
import com.cyclosa.payroll.entity.PayrollRecord;
import com.cyclosa.payroll.entity.PayrollRecordItem;
import com.cyclosa.payroll.enums.ComponentType;
import com.cyclosa.payroll.enums.PayrollPeriodStatus;
import com.cyclosa.payroll.enums.PayrollRecordStatus;
import com.cyclosa.payroll.mapper.PayrollRecordMapper;
import com.cyclosa.payroll.repository.PayrollPeriodRepository;
import com.cyclosa.payroll.repository.PayrollRecordItemRepository;
import com.cyclosa.payroll.repository.PayrollRecordRepository;
import com.cyclosa.payroll.service.PayrollCalculationEngine;
import com.cyclosa.payroll.service.PayrollRecordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PayrollRecordServiceTest {

    @Mock
    private PayrollRecordRepository recordRepository;
    @Mock
    private PayrollRecordItemRepository itemRepository;
    @Mock
    private PayrollPeriodRepository periodRepository;
    @Mock
    private EmployeeService employeeService;
    @Mock
    private SecurityPermissionEvaluator securityPermissionEvaluator;

    private PayrollCalculationEngine calculationEngine = new PayrollCalculationEngine();
    private PayrollRecordMapper recordMapper = Mappers.getMapper(PayrollRecordMapper.class);

    private PayrollRecordService service;

    private UUID companyId;
    private UUID employeeId;

    @BeforeEach
    void setUp() {
        service = new PayrollRecordService(
                recordRepository, itemRepository, periodRepository,
                calculationEngine, employeeService, securityPermissionEvaluator,
                recordMapper
        );
        companyId = UUID.randomUUID();
        employeeId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Should adjust payroll record successfully when period is OPEN")
    void testAdjustPayrollRecordSuccess() {
        UUID recordId = UUID.randomUUID();
        PayrollPeriod period = PayrollPeriod.builder()
                .companyId(companyId)
                .status(PayrollPeriodStatus.OPEN)
                .build();

        PayrollRecord record = PayrollRecord.builder()
                .companyId(companyId)
                .payrollPeriod(period)
                .employeeId(employeeId)
                .timeBasedSalary(new BigDecimal("20000000"))
                .allowancesTotal(BigDecimal.ZERO)
                .bonusTotal(BigDecimal.ZERO)
                .grossSalary(new BigDecimal("20000000"))
                .socialInsuranceEmployee(new BigDecimal("2100000"))
                .personalIncomeTax(new BigDecimal("440000"))
                .advanceDeduction(BigDecimal.ZERO)
                .otherDeductions(BigDecimal.ZERO)
                .netSalary(new BigDecimal("17460000"))
                .status(PayrollRecordStatus.DRAFT)
                .items(new ArrayList<>())
                .build();
        ReflectionTestUtils.setField(record, "id", recordId);

        when(recordRepository.findByIdAndCompanyIdWithItems(recordId, companyId)).thenReturn(Optional.of(record));
        when(recordRepository.save(any(PayrollRecord.class))).thenAnswer(inv -> inv.getArgument(0));
        when(employeeService.getEmployeeSummary(employeeId)).thenReturn(
                EmployeeSummary.builder().id(employeeId).fullName("Nguyễn Văn A").build()
        );

        AdjustPayrollRecordRequest req = AdjustPayrollRecordRequest.builder()
                .name("Thưởng dự án")
                .componentType(ComponentType.BONUS)
                .amount(new BigDecimal("2000000"))
                .isTaxable(true)
                .note("Thưởng hoàn thành xuất sắc")
                .build();

        PayrollRecordDetailResponse resp = service.adjustPayrollRecord(companyId, recordId, req);

        assertThat(resp).isNotNull();
        assertThat(record.getBonusTotal()).isEqualByComparingTo(new BigDecimal("2000000"));
        assertThat(record.getGrossSalary()).isEqualByComparingTo(new BigDecimal("22000000"));
        // Net = 22M - 2.1M - 0.44M = 19,460,000
        assertThat(record.getNetSalary()).isEqualByComparingTo(new BigDecimal("19460000.00"));
        verify(recordRepository).save(record);
    }

    @Test
    @DisplayName("Should throw exception when adjusting record in CLOSED period")
    void testAdjustPayrollRecordClosedPeriodFails() {
        UUID recordId = UUID.randomUUID();
        PayrollPeriod period = PayrollPeriod.builder()
                .companyId(companyId)
                .status(PayrollPeriodStatus.CLOSED)
                .build();

        PayrollRecord record = PayrollRecord.builder()
                .companyId(companyId)
                .payrollPeriod(period)
                .employeeId(employeeId)
                .items(new ArrayList<>())
                .build();
        ReflectionTestUtils.setField(record, "id", recordId);

        when(recordRepository.findByIdAndCompanyIdWithItems(recordId, companyId)).thenReturn(Optional.of(record));

        AdjustPayrollRecordRequest req = AdjustPayrollRecordRequest.builder()
                .name("Thưởng dự án")
                .componentType(ComponentType.BONUS)
                .amount(new BigDecimal("2000000"))
                .build();

        assertThatThrownBy(() -> service.adjustPayrollRecord(companyId, recordId, req))
                .isInstanceOf(AppException.class);
    }
}
