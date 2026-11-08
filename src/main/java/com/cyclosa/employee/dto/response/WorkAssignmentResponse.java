package com.cyclosa.employee.dto.response;

import com.cyclosa.employee.entity.enums.AssignmentChangeReason;
import com.cyclosa.employee.entity.enums.EmploymentType;
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
public class WorkAssignmentResponse {

    private UUID id;
    private UUID employeeId;
    private LocalDate effectiveFrom;

    private UUID departmentId;
    private String departmentName;
    private String departmentCode;

    private UUID branchId;
    private String branchName;
    private String branchCode;

    private UUID jobTitleId;
    private String jobTitleName;
    private String jobTitleCode;

    private UUID jobLevelId;
    private String jobLevelName;
    private Integer jobLevelRank;

    private UUID managerEmployeeId;
    private String managerEmployeeName;

    private EmploymentType employmentType;
    private AssignmentChangeReason changeReason;
    private UUID transferRequestId;
    private LocalDateTime createdAt;
}

