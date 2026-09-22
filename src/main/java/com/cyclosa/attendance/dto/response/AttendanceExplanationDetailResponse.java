package com.cyclosa.attendance.dto.response;

import com.cyclosa.attendance.enums.ExplanationReasonType;
import com.cyclosa.attendance.enums.ExplanationStatus;
import com.cyclosa.common.dto.summary.CompanySummary;
import com.cyclosa.common.dto.summary.EmployeeSummary;
import com.cyclosa.workflow.dto.response.WorkflowInstanceResponse;
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
@Schema(description = "Thông tin chi tiết chuyên sâu đơn giải trình chấm công")
public class AttendanceExplanationDetailResponse {

    private UUID id;
    private UUID companyId;
    private CompanySummary company;
    private UUID employeeId;
    private EmployeeSummary employee;
    private UUID attendanceRecordId;
    private AttendanceRecordResponse attendanceRecord;
    private LocalDate workDate;
    private ExplanationReasonType reasonType;
    private LocalTime proposedCheckIn;
    private LocalTime proposedCheckOut;
    private String reason;
    private String proofUrl;
    private UUID workflowInstanceId;
    private com.cyclosa.workflow.dto.response.WorkflowHistoryResponse workflowHistory;
    private ExplanationStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
