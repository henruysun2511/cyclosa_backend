package com.cyclosa.performance.dto.request;

import com.cyclosa.performance.enums.GoalStatus;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
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
public class UpdateGoalRequest {

    private UUID kpiId;

    private String title;

    private String targetValue;

    @DecimalMin(value = "0.01", message = "Trọng số phải lớn hơn 0")
    @DecimalMax(value = "100.00", message = "Trọng số không được vượt quá 100%")
    private BigDecimal weightPercentage;

    private GoalStatus status;

    private String description;
}
