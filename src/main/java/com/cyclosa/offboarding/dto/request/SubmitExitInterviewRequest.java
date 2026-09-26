package com.cyclosa.offboarding.dto.request;

import jakarta.validation.constraints.NotNull;
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
public class SubmitExitInterviewRequest {

    @NotNull(message = "ID nhân viên không được để trống")
    private UUID employeeId;

    private LocalDate interviewDate;

    private UUID interviewerEmployeeId;

    private String feedbackSummary;

    private Boolean wouldRecommendCompany;

    private String reasonForLeaving;

    private String suggestions;
}
