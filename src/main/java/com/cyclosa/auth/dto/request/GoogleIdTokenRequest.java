package com.cyclosa.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu đăng nhập bằng Google ID Token từ Google Sign-In SDK")
public class GoogleIdTokenRequest {

    @NotBlank(message = "Google ID Token không được để trống")
    @Schema(description = "ID Token nhận được từ Google Sign-In SDK phía frontend", example = "eyJhbGciOiJSUzI1NiIs...")
    private String idToken;
}
