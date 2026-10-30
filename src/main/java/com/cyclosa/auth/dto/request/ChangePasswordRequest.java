package com.cyclosa.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Yêu cầu đổi mật khẩu cá nhân cho người dùng đang đăng nhập")
public class ChangePasswordRequest {

    @NotBlank(message = "Mật khẩu hiện tại không được để trống")
    @Schema(description = "Mật khẩu hiện tại của tài khoản", example = "OldPassword@123")
    private String currentPassword;

    @NotBlank(message = "Mật khẩu mới không được để trống")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,100}$",
             message = "Mật khẩu phải từ 8-100 ký tự, bao gồm ít nhất 1 chữ hoa, 1 chữ thường, 1 chữ số và 1 ký tự đặc biệt")
    @Schema(description = "Mật khẩu mới muốn thay đổi", example = "NewP@ssword456")
    private String newPassword;

    @NotBlank(message = "Xác nhận mật khẩu mới không được để trống")
    @Schema(description = "Nhập lại mật khẩu mới để xác nhận", example = "NewP@ssword456")
    private String confirmNewPassword;
}
