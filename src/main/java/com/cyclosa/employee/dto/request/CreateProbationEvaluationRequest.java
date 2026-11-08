package com.cyclosa.employee.dto.request;

import com.cyclosa.employee.entity.enums.ProbationResult;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateProbationEvaluationRequest {

    @NotNull(message = "Kết quả đánh giá không được để trống")
    private ProbationResult result;

    @NotNull(message = "Ngày đánh giá không được để trống")
    private LocalDate evaluationDate;

    @NotNull(message = "Người đánh giá không được để trống")
    private UUID evaluatedByEmployeeId;

    private String comment;
}

