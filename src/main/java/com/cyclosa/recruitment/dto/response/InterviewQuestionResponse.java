package com.cyclosa.recruitment.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterviewQuestionResponse {

    private UUID id;

    private String questionText;

    private String criteria;

    private Integer maxScore;

    private BigDecimal weight;

    private Integer sortOrder;
}
