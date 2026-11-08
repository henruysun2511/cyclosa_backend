package com.cyclosa.employee.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FaceEnrollmentResponse {

    private UUID id;
    private UUID employeeId;
    private String templateRef;
    private BigDecimal qualityScore;
    private Boolean livenessVerified;
    private LocalDateTime consentGivenAt;
    private LocalDateTime enrolledAt;
    private LocalDateTime revokedAt;
    private String revokeReason;
}

