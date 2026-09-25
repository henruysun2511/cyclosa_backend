package com.cyclosa.recruitment.dto.request;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompleteAssignmentRequest {

    private BigDecimal performanceRating;

    private String evaluationNote;
}
