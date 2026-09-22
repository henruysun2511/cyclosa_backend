package com.cyclosa.attendance.dto.response;

import com.cyclosa.attendance.enums.ExplanationReasonType;
import com.cyclosa.attendance.enums.ExplanationStatus;
import com.cyclosa.common.dto.summary.EmployeeSummary;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin chi tiết đơn giải trình chấm công")
public class AttendanceExplanationResponse {

    private UUID id;
    private UUID companyId;
    private UUID employeeId;
    private EmployeeSummary employee;
    private UUID attendanceRecordId;
    private LocalDate workDate;
    private ExplanationReasonType reasonType;
    private LocalTime proposedCheckIn;
    private LocalTime proposedCheckOut;
    private String reason;
    private String proofUrl;
    private UUID workflowInstanceId;
    private ExplanationStatus status;
    private LocalDateTime createdAt;
}
