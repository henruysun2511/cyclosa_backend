package com.cyclosa.workplace.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateFloorPlanRequest {

    @NotNull(message = "Tầng (floorId) không được để trống")
    private UUID floorId;

    @NotBlank(message = "URL hình ảnh nền không được để trống")
    private String backgroundImageUrl;

    @Builder.Default
    private BigDecimal canvasWidth = BigDecimal.valueOf(3000);

    @Builder.Default
    private BigDecimal canvasHeight = BigDecimal.valueOf(2000);

    @Builder.Default
    private BigDecimal scaleRatio = BigDecimal.valueOf(0.05);

    @Builder.Default
    private Boolean isPublished = true;
}
