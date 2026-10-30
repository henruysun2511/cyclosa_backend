package com.cyclosa.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu đăng nhập bằng Google ID Token (dành cho Web/Mobile App)")
public class GoogleLoginRequest {

    @NotBlank(message = "Google ID Token không được để trống")
    @Schema(description = "ID Token nhận từ Google Identity Services", example = "eyJhbGciOiJSUzI1NiIsImtpZCI6...")
    private String idToken;
}

