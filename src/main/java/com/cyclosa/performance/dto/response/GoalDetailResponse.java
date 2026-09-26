package com.cyclosa.performance.dto.response;

import com.cyclosa.performance.enums.GoalStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoalDetailResponse {

    private UUID id;

    private UUID employeeId;

    private UUID performanceCycleId;

    private UUID kpiId;

    private String kpiName;

    private String kpiUnit;

    private String title;

    private String targetValue;

    private BigDecimal weightPercentage;

    private GoalStatus status;

    private String description;

    private PerformanceReviewResponse selfReview;

    private PerformanceReviewResponse managerReview;
}
