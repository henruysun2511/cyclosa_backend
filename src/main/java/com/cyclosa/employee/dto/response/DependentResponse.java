package com.cyclosa.employee.dto.response;

import com.cyclosa.employee.entity.enums.DependentRelationship;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DependentResponse {

    private UUID id;
    private UUID employeeId;
    private String fullName;
    private LocalDate dateOfBirth;
    private DependentRelationship relationship;
    private String identityNumber;
    private LocalDate taxDeductionFrom;
    private LocalDate taxDeductionTo;
}

