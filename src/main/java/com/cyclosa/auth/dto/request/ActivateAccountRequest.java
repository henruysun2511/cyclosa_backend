package com.cyclosa.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu kích hoạt tài khoản qua mã token 48h và đặt mật khẩu lần đầu")
public class ActivateAccountRequest {

    @NotBlank(message = "Mã token kích hoạt không được để trống")
    @Schema(description = "Mã kích hoạt nhận được qua email (có hiệu lực 48 giờ)", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private String token;

    @NotBlank(message = "Mật khẩu mới không được để trống")
    @Size(min = 8, max = 100, message = "Mật khẩu phải có độ dài từ 8 đến 100 ký tự")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
            message = "Mật khẩu phải chứa ít nhất 1 chữ hoa, 1 chữ thường, 1 chữ số và 1 ký tự đặc biệt"
    )
    @Schema(description = "Mật khẩu lần đầu thiết lập (bắt buộc đủ độ mạnh bảo mật)", example = "Password@2026")
    private String newPassword;
}

