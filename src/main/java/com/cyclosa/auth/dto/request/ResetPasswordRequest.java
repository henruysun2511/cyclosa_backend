package com.cyclosa.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu đặt lại mật khẩu mới bằng mã xác thực từ email")
public class ResetPasswordRequest {

    @NotBlank(message = "Mã xác thực không được để trống")
    @Schema(description = "Mã token khôi phục nhận được qua email", example = "550e8400-e29b-41d4-a716-446655440000")
    private String token;

    @NotBlank(message = "Mật khẩu mới không được để trống")
    @Size(min = 8, max = 100, message = "Mật khẩu tối thiểu 8 ký tự, tối đa 100 ký tự")
    @Schema(description = "Mật khẩu mới", example = "NewPassword@123")
    private String password;

    @NotBlank(message = "Xác nhận mật khẩu không được để trống")
    @Schema(description = "Nhập lại mật khẩu mới", example = "NewPassword@123")
    private String confirmPassword;
}
