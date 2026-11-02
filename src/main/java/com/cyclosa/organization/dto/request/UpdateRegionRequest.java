package com.cyclosa.organization.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu cập nhật thông tin khu vực / vùng miền")
public class UpdateRegionRequest {

    @NotBlank(message = "Tên khu vực không được để trống")
    @Size(max = 150, message = "Tên khu vực tối đa 150 ký tự")
    @Schema(description = "Tên khu vực", example = "Khu vực Miền Bắc (Mở rộng)")
    private String name;
}

