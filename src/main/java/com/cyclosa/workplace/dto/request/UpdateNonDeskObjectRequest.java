package com.cyclosa.workplace.dto.request;

import com.cyclosa.workplace.enums.NonDeskObjectType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateNonDeskObjectRequest {

    @NotNull(message = "Loại đối tượng không được để trống")
    private NonDeskObjectType objectType;

    @NotBlank(message = "Tên đối tượng không được để trống")
    @Size(max = 150, message = "Tên đối tượng không vượt quá 150 ký tự")
    private String name;

    @NotNull(message = "Tọa độ X không được để trống")
    private BigDecimal positionX;

    @NotNull(message = "Tọa độ Y không được để trống")
    private BigDecimal positionY;

    @Builder.Default
    private BigDecimal width = BigDecimal.valueOf(100);

    @Builder.Default
    private BigDecimal height = BigDecimal.valueOf(100);

    @Builder.Default
    private BigDecimal rotation = BigDecimal.ZERO;

    private Integer meetingRoomCapacity;

    private String description;
}
