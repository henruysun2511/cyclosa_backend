package com.cyclosa.workplace.dto.request;

import com.cyclosa.workplace.enums.ZoneShapeType;
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
public class CreateZoneRequest {

    @NotNull(message = "Tầng (floorId) không được để trống")
    private UUID floorId;

    @NotBlank(message = "Tên khu vực không được để trống")
    @Size(max = 150, message = "Tên khu vực không vượt quá 150 ký tự")
    private String name;

    private UUID organizationalUnitId; // Gắn phòng ban để tô màu

    @Builder.Default
    private ZoneShapeType shapeType = ZoneShapeType.RECTANGLE;

    private String boundaryCoordinates; // JSON array: [{"x": 100, "y": 200}, ...]

    @Size(max = 30, message = "Mã màu không vượt quá 30 ký tự")
    private String colorCode; // Ví dụ: "#3B82F6"

    private String description;
}
