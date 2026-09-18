package com.cyclosa.employee.dto.response;

import com.cyclosa.employee.enums.FamilyRelationship;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
public class EmployeeDependentResponse {

    private UUID id;
    private UUID employeeId;
    private String fullName;
    private FamilyRelationship relationship;
    private LocalDate dateOfBirth;
    private String nationalIdNumber;
    private boolean taxDeductionRegistered;
    private LocalDateTime createdAt;
}
