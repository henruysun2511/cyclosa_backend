package com.cyclosa.recruitment.dto.response;

import com.cyclosa.recruitment.enums.InterviewRecommendation;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluationResponse {

    private UUID id;

    private UUID interviewId;

    private UUID interviewerEmployeeId;

    private String interviewerName;

    private InterviewRecommendation overallRecommendation;

    private BigDecimal overallScore;

    private String feedbackData;

    private String notes;

    private LocalDateTime createdAt;
}
