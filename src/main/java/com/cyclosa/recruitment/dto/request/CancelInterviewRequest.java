package com.cyclosa.recruitment.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CancelInterviewRequest {

    @NotBlank(message = "Lý do hủy buổi phỏng vấn không được để trống")
    private String cancellationReason;
}
