package com.cyclosa.performance.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
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
public class GoalReviewItemRequest {

    @NotNull(message = "ID mục tiêu không được để trống")
    private UUID goalId;

    @NotNull(message = "Điểm số không được để trống")
    @DecimalMin(value = "0.00", message = "Điểm số tối thiểu là 0")
    @DecimalMax(value = "100.00", message = "Điểm số tối đa là 100")
    private BigDecimal score;

    private String comment;
}
