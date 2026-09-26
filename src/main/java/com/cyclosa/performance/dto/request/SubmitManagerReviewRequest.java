package com.cyclosa.performance.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitManagerReviewRequest {

    @NotEmpty(message = "Danh sách đánh giá mục tiêu không được để trống")
    @Valid
    private List<GoalReviewItemRequest> reviews;

    private String managerOverallComment;
}
