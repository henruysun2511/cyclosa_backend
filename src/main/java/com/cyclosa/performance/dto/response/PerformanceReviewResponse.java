package com.cyclosa.performance.dto.response;

import com.cyclosa.performance.enums.ReviewType;
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
public class PerformanceReviewResponse {

    private UUID id;

    private UUID goalId;

    private UUID evaluationId;

    private ReviewType reviewType;

    private UUID reviewerEmployeeId;

    private String reviewerEmployeeName;

    private BigDecimal score;

    private String comment;

    private LocalDateTime submittedAt;
}
