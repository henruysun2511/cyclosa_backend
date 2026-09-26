package com.cyclosa.workplace.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateFloorRequest {

    @NotBlank(message = "Số tầng / Ký hiệu tầng không được để trống")
    @Size(max = 50, message = "Số tầng không vượt quá 50 ký tự")
    private String floorNumber;

    @Size(max = 150, message = "Tên tầng không vượt quá 150 ký tự")
    private String name;

    private String description;
}
