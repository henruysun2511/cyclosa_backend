package com.cyclosa.organization.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Schema(description = "Yêu cầu tạo mới vùng miền địa lý")
public class CreateRegionRequest {

    @Schema(description = "ID công ty sở hữu", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID companyId;

    @NotBlank(message = "Mã vùng miền không được để trống")
    @Size(max = 50, message = "Mã vùng miền không vượt quá 50 ký tự")
    @Schema(description = "Mã vùng miền", example = "NORTH")
    private String code;

    @NotBlank(message = "Tên vùng miền không được để trống")
    @Size(max = 100, message = "Tên vùng miền không vượt quá 100 ký tự")
    @Schema(description = "Tên vùng miền", example = "Miền Bắc")
    private String name;
}
