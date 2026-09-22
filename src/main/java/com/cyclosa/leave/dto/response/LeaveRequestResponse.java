package com.cyclosa.leave.dto.response;

import com.cyclosa.common.dto.summary.EmployeeSummary;
import com.cyclosa.leave.enums.LeaveRequestStatus;
import com.cyclosa.leave.enums.LeaveSession;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin đơn xin nghỉ phép")
public class LeaveRequestResponse {

    private UUID id;
    private UUID companyId;
    private UUID employeeId;
    private EmployeeSummary employee;
    private UUID leaveTypeId;
    private LeaveTypeResponse leaveType;
    private LocalDate startDate;
    private LocalDate endDate;
    private LeaveSession session;
    private BigDecimal totalDays;
    private String reason;
    private LeaveRequestStatus status;
    private UUID workflowInstanceId;
    private String rejectionReason;
    private LocalDateTime approvedAt;
    private LocalDateTime cancelledAt;
    private UUID cancelledBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
