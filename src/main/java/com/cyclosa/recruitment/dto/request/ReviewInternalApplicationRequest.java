package com.cyclosa.recruitment.dto.request;

import com.cyclosa.recruitment.enums.InternalApplicationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewInternalApplicationRequest {

    @NotNull(message = "Trạng thái đánh giá không được để trống")
    private InternalApplicationStatus status;

    private String feedback;
}
