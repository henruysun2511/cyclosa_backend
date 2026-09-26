package com.cyclosa.performance.dto.request;

import com.cyclosa.performance.enums.PerformanceRating;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinalizeEvaluationRequest {

    @DecimalMin(value = "0.00", message = "Điểm số tối thiểu là 0")
    @DecimalMax(value = "100.00", message = "Điểm số tối đa là 100")
    private BigDecimal finalScore;

    private PerformanceRating rating;

    private String managerOverallComment;
}
