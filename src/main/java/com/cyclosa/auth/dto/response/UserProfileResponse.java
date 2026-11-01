package com.cyclosa.auth.dto.response;

import com.cyclosa.auth.enums.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin cá nhân tài khoản đang đăng nhập (/auth/me)")
public class UserProfileResponse {

    @Schema(description = "ID người dùng")
    private UUID id;

    @Schema(description = "Tên đăng nhập", example = "admin")
    private String username;

    @Schema(description = "Email tài khoản")
    private String email;

    @Schema(description = "Đường dẫn ảnh đại diện")
    private String avatarUrl;

    @Schema(description = "Trạng thái tài khoản")
    private UserStatus status;

    @Schema(description = "Danh sách vai trò hiện tại")
    private List<String> roles;

    @Schema(description = "Thời điểm đăng nhập gần nhất")
    private LocalDateTime lastLoginAt;

    @Schema(description = "Thời điểm tạo tài khoản")
    private LocalDateTime createdAt;
}
