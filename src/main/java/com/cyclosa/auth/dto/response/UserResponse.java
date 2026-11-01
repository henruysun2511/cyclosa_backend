package com.cyclosa.auth.dto.response;

import com.cyclosa.auth.enums.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin tài khoản người dùng trong danh sách")
public class UserResponse {

    @Schema(description = "ID người dùng")
    private UUID id;

    @Schema(description = "Tên đăng nhập của tài khoản", example = "admin")
    private String username;

    @Schema(description = "Email tài khoản", example = "admin@cyclosa.com")
    private String email;

    @Schema(description = "Trạng thái tài khoản", example = "ACTIVE")
    private UserStatus status;

    @Schema(description = "Thời điểm đăng nhập thành công gần nhất")
    private LocalDateTime lastLoginAt;

    @Schema(description = "Thời điểm tạo tài khoản")
    private LocalDateTime createdAt;
}

