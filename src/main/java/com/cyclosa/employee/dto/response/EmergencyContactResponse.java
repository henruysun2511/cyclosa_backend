package com.cyclosa.employee.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmergencyContactResponse {

    private UUID id;
    private UUID employeeId;
    private String fullName;
    private String relationship;
    private String phone;
    private String address;
}

