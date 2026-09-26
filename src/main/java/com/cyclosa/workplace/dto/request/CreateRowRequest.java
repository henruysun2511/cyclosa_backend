package com.cyclosa.workplace.dto.request;

import com.cyclosa.workplace.enums.ClusterType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateRowRequest {

    @NotNull(message = "Khu vực (zoneId) không được để trống")
    private UUID zoneId;

    @NotBlank(message = "Tên dãy bàn / cụm không được để trống")
    @Size(max = 100, message = "Tên dãy bàn không vượt quá 100 ký tự")
    private String name; // Ví dụ: "R01", "Cụm Dev 1"

    @Builder.Default
    private ClusterType clusterType = ClusterType.SINGLE_ROW;

    private BigDecimal positionX;

    private BigDecimal positionY;

    @Builder.Default
    private BigDecimal rotation = BigDecimal.ZERO;
}
