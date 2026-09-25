package com.cyclosa.auth.controller;

import com.cyclosa.role.dto.response.EffectivePermissionResponse;
import com.cyclosa.role.service.UserRoleService;
import com.cyclosa.auth.dto.request.ActivateAccountRequest;
import com.cyclosa.auth.dto.request.ChangePasswordRequest;
import com.cyclosa.auth.dto.request.ForgotPasswordRequest;
import com.cyclosa.auth.dto.request.GoogleIdTokenRequest;
import com.cyclosa.auth.dto.request.LoginRequest;
import com.cyclosa.auth.dto.request.RefreshTokenRequest;
import com.cyclosa.auth.dto.request.ResetPasswordRequest;
import com.cyclosa.auth.dto.response.TokenResponse;
import com.cyclosa.auth.dto.response.UserInfo;
import com.cyclosa.auth.service.AuthService;
import com.cyclosa.common.response.ApiResponse;
import com.cyclosa.common.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Xác thực: kích hoạt, đăng nhập, refresh token, logout, quên/đổi mật khẩu, OAuth2 Google, lấy thông tin cá nhân & quyền hạn")
public class AuthController {

    private final AuthService authService;
    private final UserRoleService userRoleService;

    @PostMapping("/activate")
    @Operation(summary = "Kích hoạt tài khoản nhân viên", description = "Nhân viên dùng mã kích hoạt từ email để đặt mật khẩu lần đầu và nhận token đăng nhập")
    public ResponseEntity<ApiResponse<TokenResponse>> activate(
            @Valid @RequestBody ActivateAccountRequest request) {

        return ResponseEntity.ok(ApiResponse.ok(
                authService.activateAccount(request),
                "Kích hoạt tài khoản và thiết lập mật khẩu thành công"));
    }

    @PostMapping("/login")
    @Operation(summary = "Đăng nhập", description = "Đăng nhập bằng email và mật khẩu, trả về cặp token")
    public ResponseEntity<ApiResponse<TokenResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        return ResponseEntity.ok(ApiResponse.ok(authService.login(request), "Đăng nhập thành công"));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Làm mới access token", description = "Dùng refresh token hợp lệ để cấp lại access token mới")
    public ResponseEntity<ApiResponse<TokenResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request) {

        return ResponseEntity.ok(ApiResponse.ok(authService.refreshToken(request), "Làm mới token thành công"));
    }

    @PostMapping("/logout")
    @Operation(summary = "Đăng xuất", description = "Blacklist access token và hủy refresh token trong Redis")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        String token = extractToken(request);
        if (StringUtils.hasText(token)) {
            authService.logout(token);
        }
        return ResponseEntity.ok(ApiResponse.noContent("Đăng xuất thành công"));
    }

    @GetMapping("/me")
    @Operation(summary = "Thông tin cá nhân", description = "Lấy thông tin tài khoản hiện tại từ Bearer token")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<UserInfo>> getCurrentUser(Authentication authentication) {
        UUID userId = SecurityUtils.currentUserId(authentication);
        return ResponseEntity.ok(ApiResponse.ok(authService.getCurrentUser(userId), "Lấy thông tin tài khoản thành công"));
    }

    @GetMapping("/me/permissions")
    @Operation(summary = "Quyền hạn của tôi", description = "Lấy danh sách các quyền và phạm vi dữ liệu tối đa của tài khoản đang đăng nhập")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<List<EffectivePermissionResponse>>> getMyPermissions(Authentication authentication) {
        UUID userId = SecurityUtils.currentUserId(authentication);
        return ResponseEntity.ok(ApiResponse.ok(userRoleService.getEffectivePermissions(userId), "Lấy danh sách quyền hạn thành công"));
    }

    // ==========================================
    // Forgot Password — Gửi email khôi phục
    // ==========================================
    @PostMapping("/forgot-password")
    @Operation(summary = "Quên mật khẩu", description = "Gửi email chứa liên kết đặt lại mật khẩu. API luôn trả về thành công bất kể email có tồn tại hay không (bảo mật chống dò email).")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {

        authService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.noContent("Nếu email tồn tại trong hệ thống, chúng tôi đã gửi hướng dẫn đặt lại mật khẩu"));
    }

    // ==========================================
    // Reset Password — Đặt lại mật khẩu mới
    // ==========================================
    @PostMapping("/reset-password")
    @Operation(summary = "Đặt lại mật khẩu", description = "Sử dụng mã xác thực từ email để đặt mật khẩu mới. Sau khi đặt lại, phiên đăng nhập cũ sẽ bị hủy.")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.noContent("Đặt lại mật khẩu thành công, vui lòng đăng nhập lại"));
    }

    // ==========================================
    // Change Password — Đổi mật khẩu cá nhân
    // ==========================================
    @PutMapping("/change-password")
    @Operation(summary = "Đổi mật khẩu", description = "Người dùng đang đăng nhập đổi mật khẩu cá nhân. Yêu cầu nhập đúng mật khẩu hiện tại. Sau khi đổi, phiên đăng nhập cũ sẽ bị hủy.")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request) {

        UUID userId = SecurityUtils.currentUserId(authentication);
        authService.changePassword(userId, request);
        return ResponseEntity.ok(ApiResponse.noContent("Đổi mật khẩu thành công, vui lòng đăng nhập lại"));
    }

    // ==========================================
    // Google OAuth2 — Đăng nhập bằng Google ID Token
    // ==========================================
    @PostMapping("/oauth2/google")
    @Operation(summary = "Đăng nhập bằng Google", description = "Xác thực bằng Google ID Token nhận từ Google Sign-In SDK phía frontend, trả về cặp token hệ thống")
    public ResponseEntity<ApiResponse<TokenResponse>> googleLogin(
            @Valid @RequestBody GoogleIdTokenRequest request) {

        return ResponseEntity.ok(ApiResponse.ok(
                authService.googleIdTokenLogin(request),
                "Đăng nhập bằng Google thành công"));
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
