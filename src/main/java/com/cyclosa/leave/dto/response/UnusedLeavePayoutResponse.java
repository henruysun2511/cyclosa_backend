package com.cyclosa.leave.dto.response;

import com.cyclosa.common.dto.summary.EmployeeSummary;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO kết quả tính toán chi trả tiền phép năm chưa dùng khi chấm dứt HĐLĐ (Điều 113.3 BLLĐ 2019).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Kết quả tính thanh toán phép năm chưa nghỉ khi thôi việc (Điều 113.3 BLLĐ)")
public class UnusedLeavePayoutResponse {

    private UUID employeeId;
    private EmployeeSummary employee;
    private Integer year;
    private BigDecimal remainingAnnualLeaveDays;
    private BigDecimal basicSalary;
    private BigDecimal dailyRate;
    private BigDecimal totalPayoutAmount;
}
