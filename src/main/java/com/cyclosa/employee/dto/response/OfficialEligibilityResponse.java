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
public class OfficialEligibilityResponse {

    private UUID employeeId;
    private boolean eligible;
    private boolean hasPassedEvaluation;
    private boolean isOnboardingCompleted;
    private boolean hasOfficialContract;
    private String reason;
}

