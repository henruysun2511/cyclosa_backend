package com.cyclosa.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Kết quả xác thực thành công chứa Token và thông tin người dùng")
public class AuthTokenResponse {

    @Schema(description = "Access Token dạng JWT")
    private String accessToken;

    @Schema(description = "Refresh Token lưu trên Redis")
    private String refreshToken;

    @Schema(description = "Loại token", example = "Bearer")
    @Builder.Default
    private String tokenType = "Bearer";

    @Schema(description = "Thời gian sống của Access Token tính theo mili-giây", example = "86400000")
    private long expiresIn;

    @Schema(description = "ID người dùng")
    private UUID userId;

    @Schema(description = "Email tài khoản")
    private String email;

    @Schema(description = "Danh sách vai trò của người dùng", example = "[\"EMPLOYEE\", \"HR_ADMIN\"]")
    private List<String> roles;
}

