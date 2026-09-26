package com.cyclosa.offboarding.dto.response;

import com.cyclosa.common.dto.summary.CompanySummary;
import com.cyclosa.common.dto.summary.EmployeeSummary;
import com.cyclosa.common.dto.summary.OrgUnitSummary;
import com.cyclosa.offboarding.enums.TerminationReason;
import com.cyclosa.offboarding.enums.TerminationStatus;
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
public class TerminationResponse {

    private UUID id;

    private UUID employeeId;

    private String employeeName;

    private String employeeCode;

    private EmployeeSummary employee;

    private String departmentName;

    private OrgUnitSummary department;

    private UUID companyId;

    private CompanySummary company;

    private LocalDate decisionDate;

    private LocalDate lastWorkingDate;

    private TerminationReason decisionReasonCategory;

    private UUID decidedByEmployeeId;

    private String decidedByEmployeeName;

    private EmployeeSummary decidedByEmployee;

    private UUID disciplineId;

    private UUID contractTerminationId;

    private String reasonDetail;

    private TerminationStatus status;

    private LocalDateTime createdAt;
}
