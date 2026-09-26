package com.cyclosa.workplace.dto.request;

import com.cyclosa.workplace.enums.ClusterType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateRowRequest {

    @NotBlank(message = "Tên dãy bàn / cụm không được để trống")
    @Size(max = 100, message = "Tên dãy bàn không vượt quá 100 ký tự")
    private String name;

    @Builder.Default
    private ClusterType clusterType = ClusterType.SINGLE_ROW;

    private BigDecimal positionX;

    private BigDecimal positionY;

    @Builder.Default
    private BigDecimal rotation = BigDecimal.ZERO;
}
