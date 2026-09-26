package com.cyclosa.discipline.dto.response;

import com.cyclosa.common.dto.summary.CompanySummary;
import com.cyclosa.common.dto.summary.EmployeeSummary;
import com.cyclosa.common.dto.summary.OrgUnitSummary;
import com.cyclosa.discipline.enums.DisciplineStatus;
import com.cyclosa.discipline.enums.DisciplineType;
import com.cyclosa.discipline.enums.DismissalGround;
import com.cyclosa.discipline.enums.ViolationCategory;
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
public class DisciplineResponse {

    private UUID id;

    private UUID employeeId;

    private String employeeName;

    private String employeeCode;

    private EmployeeSummary employee;

    private String departmentName;

    private OrgUnitSummary department;

    private UUID companyId;

    private CompanySummary company;

    private LocalDate violationDate;

    private ViolationCategory violationCategory;

    private LocalDate statuteOfLimitationDeadline;

    private String evidenceFiles;

    private String handbookReference;

    private LocalDate meetingDate;

    private String meetingAttendees;

    private DisciplineType disciplineType;

    private DismissalGround dismissalGround;

    private Integer salaryExtensionMonths;

    private String reason;

    private LocalDate decisionDate;

    private UUID decidedByEmployeeId;

    private String decidedByEmployeeName;

    private EmployeeSummary decidedBy;

    private DisciplineStatus status;

    private LocalDate expiryDate;

    private String fileUrl;

    private LocalDateTime createdAt;
}
