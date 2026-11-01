package com.cyclosa.authorization.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Yêu cầu cập nhật thông tin vai trò")
public class UpdateRoleRequest {

    @Schema(description = "Tên vai trò hiển thị mới", example = "Trưởng phòng Kinh doanh Khu vực")
    @NotBlank(message = "Tên vai trò không được để trống")
    @Size(max = 100, message = "Tên vai trò không được vượt quá 100 ký tự")
    private String name;

    @Schema(description = "Mô tả chi tiết quyền hạn và trách nhiệm", example = "Phụ trách doanh số toàn bộ chi nhánh miền Nam")
    private String description;
}

