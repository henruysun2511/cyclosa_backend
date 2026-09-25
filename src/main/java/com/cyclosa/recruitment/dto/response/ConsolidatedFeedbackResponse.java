package com.cyclosa.recruitment.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsolidatedFeedbackResponse {

    private UUID interviewId;

    private int totalPanelMembers;

    private int submittedEvaluations;

    private BigDecimal averageScore;

    private boolean divergenceAlert;

    private String divergenceMessage;

    private List<EvaluationResponse> evaluations;
}
