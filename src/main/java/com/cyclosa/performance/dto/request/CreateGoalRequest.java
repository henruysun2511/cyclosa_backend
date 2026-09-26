package com.cyclosa.performance.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
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
public class CreateGoalRequest {

    @NotNull(message = "ID nhân viên không được để trống")
    private UUID employeeId;

    @NotNull(message = "ID kỳ đánh giá không được để trống")
    private UUID performanceCycleId;

    private UUID kpiId;

    @NotBlank(message = "Tiêu đề mục tiêu không được để trống")
    private String title;

    @NotBlank(message = "Chỉ tiêu mục tiêu (Target value) không được để trống")
    private String targetValue;

    @NotNull(message = "Trọng số không được để trống")
    @DecimalMin(value = "0.01", message = "Trọng số phải lớn hơn 0")
    @DecimalMax(value = "100.00", message = "Trọng số không được vượt quá 100%")
    private BigDecimal weightPercentage;

    private String description;
}
