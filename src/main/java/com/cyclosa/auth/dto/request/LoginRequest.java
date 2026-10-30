package com.cyclosa.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu đăng nhập bằng Email và Mật khẩu")
public class LoginRequest {

    @NotBlank(message = "Email đăng nhập không được để trống")
    @Email(message = "Định dạng email không hợp lệ")
    @Schema(description = "Email tài khoản công ty hoặc cá nhân", example = "admin@cyclosa.com")
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, max = 100, message = "Mật khẩu phải từ 6 đến 100 ký tự")
    @Schema(description = "Mật khẩu tài khoản", example = "Admin@123456")
    private String password;
}

