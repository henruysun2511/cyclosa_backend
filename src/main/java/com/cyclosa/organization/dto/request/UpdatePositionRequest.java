package com.cyclosa.organization.dto.request;

import com.cyclosa.common.enums.ActiveStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Schema(description = "Yêu cầu cập nhật thông tin vị trí chức danh")
public class UpdatePositionRequest {

    @NotBlank(message = "Tên chức danh không được để trống")
    @Size(max = 150, message = "Tên chức danh không vượt quá 150 ký tự")
    @Schema(description = "Tên chức danh", example = "Kỹ sư Lập trình Backend Senior")
    private String name;

    @Schema(description = "ID cấp bậc công việc tương ứng", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID jobLevelId;

    @Schema(description = "Mô tả tiêu chuẩn và nhiệm vụ công việc", example = "Phát triển các API backend sử dụng Java Spring Boot")
    private String description;

    @NotNull(message = "Trạng thái hoạt động không được để trống")
    @Schema(description = "Trạng thái hoạt động", example = "ACTIVE")
    private ActiveStatus status;
}
