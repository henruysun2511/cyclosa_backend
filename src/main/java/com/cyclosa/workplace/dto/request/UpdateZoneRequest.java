package com.cyclosa.workplace.dto.request;

import com.cyclosa.workplace.enums.ZoneShapeType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateZoneRequest {

    @NotBlank(message = "Tên khu vực không được để trống")
    @Size(max = 150, message = "Tên khu vực không vượt quá 150 ký tự")
    private String name;

    private UUID organizationalUnitId;

    @Builder.Default
    private ZoneShapeType shapeType = ZoneShapeType.RECTANGLE;

    private String boundaryCoordinates;

    @Size(max = 30, message = "Mã màu không vượt quá 30 ký tự")
    private String colorCode;

    private String description;
}
