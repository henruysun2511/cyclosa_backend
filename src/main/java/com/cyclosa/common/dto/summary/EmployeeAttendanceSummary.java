package com.cyclosa.common.dto.summary;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Bảng tổng hợp công tháng của nhân viên cung cấp cho Module Payroll")
public class EmployeeAttendanceSummary {

    @Schema(description = "ID nhân viên")
    private UUID employeeId;

    @Schema(description = "Tháng tính công", example = "10")
    private int month;

    @Schema(description = "Năm tính công", example = "2026")
    private int year;

    @Schema(description = "Số công chuẩn trong tháng (VD: 22.0 hoặc 26.0)", example = "22.0")
    private BigDecimal standardWorkDays;

    @Schema(description = "Số công thực tế đi làm (VD: 20.5)", example = "20.5")
    private BigDecimal actualWorkDays;

    @Schema(description = "Số ngày nghỉ phép hưởng 100% lương", example = "1.0")
    private BigDecimal paidLeaveDays;

    @Schema(description = "Tổng công hưởng lương = actualWorkDays + paidLeaveDays", example = "21.5")
    private BigDecimal totalPaidWorkDays;

    @Schema(description = "Tổng số phút đi muộn trong tháng", example = "15")
    private int totalLateMinutes;

    @Schema(description = "Tổng số phút về sớm trong tháng", example = "0")
    private int totalEarlyMinutes;

    @Schema(description = "Số lần vi phạm quên chấm công trong tháng", example = "0")
    private int missingPunchCount;
}
