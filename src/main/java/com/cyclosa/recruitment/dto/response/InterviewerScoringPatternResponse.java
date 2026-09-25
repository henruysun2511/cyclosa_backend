package com.cyclosa.recruitment.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterviewerScoringPatternResponse {

    private UUID interviewerEmployeeId;

    private String interviewerName;

    private int totalEvaluations;

    private BigDecimal averageScore;

    private Map<String, Long> recommendationDistribution; // STRONG_YES: 5, YES: 12, NO: 3, STRONG_NO: 1
}
