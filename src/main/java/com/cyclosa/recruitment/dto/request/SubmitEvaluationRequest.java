package com.cyclosa.recruitment.dto.request;

import com.cyclosa.recruitment.enums.InterviewRecommendation;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmitEvaluationRequest {

    private UUID interviewerEmployeeId;

    @NotNull(message = "Khuyến nghị phỏng vấn không được để trống")
    private InterviewRecommendation overallRecommendation;

    private BigDecimal overallScore;

    private String feedbackData; // JSON chi tiết từng tiêu chí Scorecard

    private String notes;
}
