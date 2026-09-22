package com.cyclosa.attendance.dto.response;

import com.cyclosa.common.dto.summary.EmployeeSummary;
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
@Schema(description = "Bảng tổng hợp công tháng")
public class MonthlyTimesheetResponse {

    private UUID id;
    private UUID companyId;
    private UUID employeeId;
    private EmployeeSummary employee;
    private Integer month;
    private Integer year;
    private BigDecimal standardWorkDays;
    private BigDecimal actualWorkDays;
    private BigDecimal paidLeaveDays;
    private BigDecimal unpaidLeaveDays;
    private BigDecimal totalPaidDays;
    private Integer totalLateMinutes;
    private Integer totalEarlyMinutes;
    private Integer missingPunchCount;
    private Boolean isLocked;
    private LocalDateTime createdAt;
}
