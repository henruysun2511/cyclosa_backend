package com.cyclosa.payroll.dto.response;

import com.cyclosa.common.dto.summary.EmployeeSummary;
import com.cyclosa.payroll.enums.PayrollRecordStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Bản ghi lương nhân sự trong kỳ")
public class PayrollRecordResponse {

    private UUID id;
    private UUID companyId;
    private UUID payrollPeriodId;
    private UUID employeeId;
    private EmployeeSummary employee;

    private BigDecimal basicSalary;
    private BigDecimal insuranceSalary;
    private BigDecimal standardWorkDays;
    private BigDecimal actualWorkDays;
    private BigDecimal paidLeaveDays;
    private BigDecimal unpaidLeaveDays;

    private BigDecimal timeBasedSalary;
    private BigDecimal overtimePay;
    private BigDecimal allowancesTotal;
    private BigDecimal bonusTotal;
    private BigDecimal grossSalary;

    private BigDecimal socialInsuranceEmployee;
    private BigDecimal socialInsuranceEmployer;

    private BigDecimal taxableIncome;
    private Integer dependentsCount;
    private BigDecimal personalReliefAmount;
    private BigDecimal dependentReliefAmount;
    private BigDecimal assessedIncome;
    private BigDecimal personalIncomeTax;

    private BigDecimal advanceDeduction;
    private BigDecimal otherDeductions;
    private BigDecimal netSalary;

    private String bankAccountNumber;
    private String bankName;
    private String bankBranch;

    private PayrollRecordStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
