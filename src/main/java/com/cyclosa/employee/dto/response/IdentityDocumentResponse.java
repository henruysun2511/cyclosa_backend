package com.cyclosa.employee.dto.response;

import com.cyclosa.employee.entity.enums.IdentityDocumentType;
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
public class IdentityDocumentResponse {

    private UUID id;
    private UUID employeeId;
    private IdentityDocumentType documentType;
    private String documentNumber;
    private LocalDate issueDate;
    private String issuePlace;
    private LocalDate expiryDate;
}

