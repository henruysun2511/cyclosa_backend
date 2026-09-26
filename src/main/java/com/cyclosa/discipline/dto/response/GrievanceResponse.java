package com.cyclosa.discipline.dto.response;

import com.cyclosa.common.dto.summary.CompanySummary;
import com.cyclosa.common.dto.summary.EmployeeSummary;
import com.cyclosa.common.dto.summary.OrgUnitSummary;
import com.cyclosa.discipline.enums.GrievanceCategory;
import com.cyclosa.discipline.enums.GrievanceStatus;
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
public class GrievanceResponse {

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

    private GrievanceCategory category;

    private String description;

    private GrievanceStatus status;

    private String resolutionNote;

    private UUID resolvedByEmployeeId;

    private String resolvedByEmployeeName;

    private EmployeeSummary resolvedBy;

    private LocalDateTime resolvedAt;

    private LocalDateTime createdAt;
}
