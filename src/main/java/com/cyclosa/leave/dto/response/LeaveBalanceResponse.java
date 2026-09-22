package com.cyclosa.leave.dto.response;

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
@Schema(description = "Thông tin quỹ phép của nhân viên")
public class LeaveBalanceResponse {

    private UUID id;
    private UUID companyId;
    private UUID employeeId;
    private EmployeeSummary employee;
    private UUID leaveTypeId;
    private LeaveTypeResponse leaveType;
    private Integer year;
    private BigDecimal totalDays;
    private BigDecimal carriedOverDays;
    private BigDecimal usedDays;
    private BigDecimal pendingDays;
    private BigDecimal remainingDays;
    private BigDecimal availableDays;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
