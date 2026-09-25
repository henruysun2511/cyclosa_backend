package com.cyclosa.common.dto.summary;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin tóm tắt người dùng (dùng cho audit log, người tạo, người sửa...)")
public class UserSummary {

    @Schema(description = "ID người dùng")
    private UUID id;

    @Schema(description = "Tên đăng nhập", example = "admin")
    private String username;

    @Schema(description = "Họ và tên", example = "Quản trị hệ thống")
    private String fullName;

    @Schema(description = "Email", example = "admin@cyclosa.com")
    private String email;

    @Schema(description = "Ảnh đại diện")
    private String avatarUrl;
}
