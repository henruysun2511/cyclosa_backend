package com.cyclosa.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu cấp lại Access Token mới bằng Refresh Token")
public class RefreshTokenRequest {

    @NotBlank(message = "Refresh Token không được để trống")
    @Schema(description = "Refresh Token hợp lệ do hệ thống cấp", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String refreshToken;
}

