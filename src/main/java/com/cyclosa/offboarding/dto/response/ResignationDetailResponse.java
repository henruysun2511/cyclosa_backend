package com.cyclosa.offboarding.dto.response;

import com.cyclosa.common.dto.summary.CompanySummary;
import com.cyclosa.common.dto.summary.EmployeeSummary;
import com.cyclosa.common.dto.summary.OrgUnitSummary;
import com.cyclosa.offboarding.enums.ResignationReason;
import com.cyclosa.offboarding.enums.ResignationStatus;
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
@Schema(description = "Chi tiết đơn xin thôi việc của nhân viên")
public class ResignationDetailResponse {

    private UUID id;
    private UUID employeeId;
    private String employeeName;
    private String employeeCode;
    private EmployeeSummary employee;

    private String departmentName;
    private OrgUnitSummary department;

    private UUID companyId;
    private CompanySummary company;

    private LocalDate submittedDate;
    private LocalDate expectedLastWorkingDate;
    private ResignationReason personalReasonCategory;
    private String reasonDetail;
    private ResignationStatus status;

    private UUID approvedByEmployeeId;
    private String approvedByEmployeeName;
    private EmployeeSummary approvedByEmployee;

    private LocalDateTime approvedAt;
    private String rejectionReason;
    private UUID contractTerminationId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
