package com.cyclosa.authorization.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Schema(description = "Yêu cầu tạo vai trò mới")
public class CreateRoleRequest {

    @Schema(description = "Mã vai trò (in hoa, viết liền hoặc gạch dưới)", example = "SALES_LEAD")
    @NotBlank(message = "Mã vai trò không được để trống")
    @Size(max = 50, message = "Mã vai trò không được vượt quá 50 ký tự")
    @Pattern(regexp = "^[A-Z0-9_]+$", message = "Mã vai trò chỉ được chứa chữ hoa, số và dấu gạch dưới")
    private String code;

    @Schema(description = "Tên vai trò hiển thị", example = "Trưởng nhóm Bán hàng")
    @NotBlank(message = "Tên vai trò không được để trống")
    @Size(max = 100, message = "Tên vai trò không được vượt quá 100 ký tự")
    private String name;

    @Schema(description = "Mô tả chi tiết quyền hạn và trách nhiệm", example = "Quản lý chỉ tiêu bán hàng và phê duyệt báo giá")
    private String description;

    @Schema(description = "Pháp nhân công ty sở hữu vai trò này (NULL nếu là quản trị tối cao)", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID companyId;
}

