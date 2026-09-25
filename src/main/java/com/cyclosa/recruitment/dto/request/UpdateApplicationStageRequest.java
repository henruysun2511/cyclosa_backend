package com.cyclosa.recruitment.dto.request;

import com.cyclosa.recruitment.enums.ApplicationStage;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateApplicationStageRequest {
    @NotNull(message = "Giai đoạn tuyển dụng không được để trống")
    private ApplicationStage stage;
    private String rejectionReason;
    private BigDecimal scoreMatch;
}
