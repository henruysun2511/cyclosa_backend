package com.cyclosa.employee.dto.response;

import com.cyclosa.employee.enums.FamilyRelationship;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
public class EmployeeEmergencyContactResponse {

    private UUID id;
    private UUID employeeId;
    private String fullName;
    private FamilyRelationship relationship;
    private String phone;
    private String address;
    private LocalDateTime createdAt;
}
