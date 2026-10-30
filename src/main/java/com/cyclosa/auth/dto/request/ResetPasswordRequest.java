package com.cyclosa.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu đặt lại mật khẩu mới bằng mã OTP")
public class ResetPasswordRequest {

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    @Schema(description = "Email tài khoản cần đặt lại mật khẩu", example = "user@cyclosa.com")
    private String email;

    @NotBlank(message = "Mã OTP không được để trống")
    @Pattern(regexp = "^\\d{6}$", message = "Mã OTP phải gồm 6 chữ số")
    @Schema(description = "Mã OTP 6 chữ số nhận được qua email", example = "123456")
    private String otp;

    @NotBlank(message = "Mật khẩu mới không được để trống")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,100}$",
             message = "Mật khẩu phải từ 8-100 ký tự, bao gồm ít nhất 1 chữ hoa, 1 chữ thường, 1 chữ số và 1 ký tự đặc biệt")
    @Schema(description = "Mật khẩu mới thỏa mãn chính sách bảo mật", example = "NewP@ssword123")
    private String newPassword;

    @NotBlank(message = "Xác nhận mật khẩu mới không được để trống")
    @Schema(description = "Nhập lại mật khẩu mới để xác nhận", example = "NewP@ssword123")
    private String confirmPassword;
}
