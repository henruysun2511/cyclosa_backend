package com.cyclosa.auth.controller;

import com.cyclosa.auth.dto.request.*;
import com.cyclosa.auth.dto.response.AuthTokenResponse;
import com.cyclosa.auth.dto.response.UserProfileResponse;
import com.cyclosa.auth.service.AuthService;
import com.cyclosa.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Các API xác thực, kích hoạt tài khoản 48h, làm mới phiên, quên mật khẩu OTP và đổi mật khẩu")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Đăng nhập bằng Tên đăng nhập hoặc Email và Mật khẩu", description = "Kiểm tra tài khoản theo username hoặc email, trả về cặp Access Token và Refresh Token, cập nhật last_login_at")
    public ApiResponse<AuthTokenResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthTokenResponse response = authService.login(request);
        return ApiResponse.ok(response);
    }

    @PostMapping("/activate")
    @Operation(summary = "Kích hoạt tài khoản nhân viên", description = "Nhập mã token nhận qua email (48 giờ) và thiết lập mật khẩu lần đầu. Tự động đăng nhập.")
    public ApiResponse<AuthTokenResponse> activateAccount(@Valid @RequestBody ActivateAccountRequest request) {
        AuthTokenResponse response = authService.activateAccount(request);
        return ApiResponse.ok(response);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Làm mới Access Token", description = "Cấp Access Token mới từ Refresh Token hợp lệ (đối chiếu dữ liệu Redis)")
    public ApiResponse<AuthTokenResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        AuthTokenResponse response = authService.refreshToken(request);
        return ApiResponse.ok(response);
    }

    @PostMapping("/logout")
    @Operation(summary = "Đăng xuất tài khoản", description = "Đưa Access Token vào Redis Blacklist và xóa Refresh Token để hủy phiên làm việc")
    public ApiResponse<Void> logout(@RequestHeader(value = "Authorization", required = false) String bearerToken) {
        authService.logout(bearerToken);
        return ApiResponse.ok(null);
    }

    @GetMapping("/me")
    @Operation(summary = "Lấy thông tin tài khoản đang đăng nhập", description = "Trả về thông tin User, email, trạng thái và vai trò hiện tại")
    public ApiResponse<UserProfileResponse> getCurrentUser() {
        UserProfileResponse response = authService.getCurrentUser();
        return ApiResponse.ok(response);
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Yêu cầu mã OTP khôi phục mật khẩu", description = "Gửi mã OTP 6 chữ số có hiệu lực 15 phút tới email của người dùng")
    public ApiResponse<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ApiResponse.ok(null);
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Đặt lại mật khẩu mới bằng mã OTP", description = "Xác thực mã OTP 6 chữ số và cập nhật mật khẩu mới. Hủy toàn bộ phiên làm việc cũ.")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ApiResponse.ok(null);
    }

    @PutMapping("/change-password")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Đổi mật khẩu người dùng đang đăng nhập", description = "Yêu cầu nhập đúng mật khẩu hiện tại và thiết lập mật khẩu mới. Cưỡng chế đăng nhập lại.")
    public ApiResponse<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(request);
        return ApiResponse.ok(null);
    }
}
