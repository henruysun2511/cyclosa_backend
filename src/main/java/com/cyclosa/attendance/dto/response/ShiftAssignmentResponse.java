package com.cyclosa.attendance.dto.response;

import com.cyclosa.attendance.enums.ShiftAssignmentStatus;
import com.cyclosa.common.dto.summary.EmployeeSummary;
import com.cyclosa.common.dto.summary.ShiftSummary;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin phân ca làm việc")
public class ShiftAssignmentResponse {

    private UUID id;
    private UUID companyId;
    private UUID employeeId;
    private EmployeeSummary employee;
    private ShiftSummary shift;
    private LocalDate assignedDate;
    private ShiftAssignmentStatus status;
    private String note;
    private LocalDateTime createdAt;
}
