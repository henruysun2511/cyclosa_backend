package com.cyclosa.payroll.dto.response;

import com.cyclosa.common.dto.summary.EmployeeSummary;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Phiếu lương cá nhân (Payslip)")
public class PayslipResponse {

    private UUID recordId;
    private UUID payrollPeriodId;
    private String periodName;
    private Integer month;
    private Integer year;
    private EmployeeSummary employee;

    private BigDecimal basicSalary;
    private BigDecimal standardWorkDays;
    private BigDecimal actualWorkDays;
    private BigDecimal paidLeaveDays;

    private BigDecimal timeBasedSalary;
    private BigDecimal allowancesTotal;
    private BigDecimal bonusTotal;
    private BigDecimal grossSalary;

    private BigDecimal socialInsuranceEmployee;
    private BigDecimal personalIncomeTax;
    private BigDecimal advanceDeduction;
    private BigDecimal otherDeductions;
    private BigDecimal netSalary;

    private List<PayrollRecordItemResponse> items;
}
