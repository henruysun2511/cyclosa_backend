package com.cyclosa.workplace.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateFloorRequest {

    @NotNull(message = "Tòa nhà (siteId) không được để trống")
    private UUID siteId;

    @NotBlank(message = "Số tầng / Ký hiệu tầng không được để trống")
    @Size(max = 50, message = "Số tầng không vượt quá 50 ký tự")
    private String floorNumber; // Ví dụ: "8", "F8", "Tầng trệt"

    @Size(max = 150, message = "Tên tầng không vượt quá 150 ký tự")
    private String name;

    private String description;
}
