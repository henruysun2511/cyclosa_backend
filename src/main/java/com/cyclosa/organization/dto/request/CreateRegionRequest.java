package com.cyclosa.organization.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu tạo mới khu vực / vùng miền")
public class CreateRegionRequest {

    @NotNull(message = "ID công ty không được để trống")
    @Schema(description = "ID công ty trực thuộc", example = "00000000-0000-0000-0000-000000000001")
    private UUID companyId;

    @NotBlank(message = "Mã khu vực không được để trống")
    @Size(max = 50, message = "Mã khu vực tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Z0-9_-]+$", message = "Mã khu vực chỉ chứa chữ hoa, số, gạch dưới và gạch ngang")
    @Schema(description = "Mã khu vực", example = "REG_NORTH")
    private String code;

    @NotBlank(message = "Tên khu vực không được để trống")
    @Size(max = 150, message = "Tên khu vực tối đa 150 ký tự")
    @Schema(description = "Tên khu vực", example = "Khu vực Miền Bắc")
    private String name;
}

