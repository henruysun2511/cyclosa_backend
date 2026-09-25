package com.cyclosa.recruitment.dto.response;

import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConvertToEmployeeResponse {

    private UUID applicationId;

    private UUID candidateId;

    private UUID employeeId;

    private String employeeCode;

    private String fullName;

    private String companyEmail;

    private LocalDate hireDate;

    private String message;
}
