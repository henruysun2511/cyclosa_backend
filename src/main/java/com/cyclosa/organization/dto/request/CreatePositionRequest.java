package com.cyclosa.organization.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Schema(description = "Yêu cầu tạo mới vị trí chức danh")
public class CreatePositionRequest {

    @Schema(description = "ID công ty sở hữu", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID companyId;

    @NotBlank(message = "Mã vị trí không được để trống")
    @Size(max = 50, message = "Mã vị trí không vượt quá 50 ký tự")
    @Schema(description = "Mã vị trí chức danh", example = "POS_BACKEND_DEV")
    private String code;

    @NotBlank(message = "Tên chức danh không được để trống")
    @Size(max = 150, message = "Tên chức danh không vượt quá 150 ký tự")
    @Schema(description = "Tên chức danh", example = "Kỹ sư Lập trình Backend")
    private String name;

    @Schema(description = "ID cấp bậc công việc tương ứng", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID jobLevelId;

    @Schema(description = "Mô tả tiêu chuẩn và nhiệm vụ công việc", example = "Phát triển các API backend sử dụng Java Spring Boot")
    private String description;
}
