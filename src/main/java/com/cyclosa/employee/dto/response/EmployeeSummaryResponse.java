package com.cyclosa.employee.dto.response;

import com.cyclosa.employee.entity.enums.EmployeeStatus;
import com.cyclosa.employee.entity.enums.EmploymentType;
import com.cyclosa.employee.entity.enums.Gender;
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
public class EmployeeSummaryResponse {

    private UUID id;
    private UUID companyId;
    private UUID userId;
    private String employeeCode;
    private String fullName;
    private String lastName;
    private String firstName;
    private LocalDate dateOfBirth;
    private Gender gender;
    private String phone;
    private String personalEmail;
    private LocalDate startDate;
    private LocalDate probationEndDate;
    private String portraitUrl;

    // Chiều 1: Tổ chức
    private UUID currentDepartmentId;
    private String currentDepartmentName;

    // Chiều 2: Địa lý
    private UUID currentBranchId;
    private String currentBranchName;

    // Chiều 3: Chức danh & Cấp bậc
    private UUID currentJobTitleId;
    private String currentJobTitleName;
    private UUID currentJobLevelId;
    private String currentJobLevelName;

    // Quản lý trực tiếp
    private UUID currentManagerId;
    private String currentManagerName;

    private EmploymentType currentEmploymentType;
    private EmployeeStatus currentStatus;
    private LocalDateTime createdAt;
}

