package com.cyclosa.auth.dto.response;

import com.cyclosa.auth.enums.OAuthProvider;
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
@Schema(description = "Thông tin chi tiết tài khoản người dùng")
public class UserDetailResponse {

    @Schema(description = "ID người dùng")
    private UUID id;

    @Schema(description = "Email tài khoản", example = "admin@cyclosa.com")
    private String email;

    @Schema(description = "Trạng thái tài khoản", example = "ACTIVE")
    private UserStatus status;

    @Schema(description = "Thời điểm đăng nhập thành công gần nhất")
    private LocalDateTime lastLoginAt;

    @Schema(description = "Danh sách nhà cung cấp OAuth2 đã liên kết", example = "[\"GOOGLE\"]")
    private List<OAuthProvider> linkedProviders;

    @Schema(description = "Cờ xóa mềm", example = "false")
    private Boolean deleted;

    @Schema(description = "Thời điểm tạo tài khoản")
    private LocalDateTime createdAt;

    @Schema(description = "Thời điểm cập nhật gần nhất")
    private LocalDateTime updatedAt;
}

