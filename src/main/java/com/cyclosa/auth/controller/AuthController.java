package com.cyclosa.auth.controller;

import com.cyclosa.role.dto.response.EffectivePermissionResponse;
import com.cyclosa.role.service.UserRoleService;
import com.cyclosa.auth.dto.request.LoginRequest;
import com.cyclosa.auth.dto.request.RefreshTokenRequest;
import com.cyclosa.auth.dto.request.RegisterRequest;
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
@Tag(name = "Authentication", description = "Xác thực: đăng ký, đăng nhập, refresh token, logout, lấy thông tin cá nhân & quyền hạn")
public class AuthController {

    private final AuthService authService;
    private final UserRoleService userRoleService;

    @PostMapping("/register")
    @Operation(summary = "Đăng ký tài khoản", description = "Tạo tài khoản EMPLOYEE mới và trả về access token & refresh token")
    public ResponseEntity<ApiResponse<TokenResponse>> register(
            @Valid @RequestBody RegisterRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(
                        authService.register(request),
                        "Đăng ký tài khoản thành công"));
    }

    @PostMapping("/login")
    @Operation(summary = "Đăng nhập", description = "Đăng nhập bằng email và mật khẩu, trả về cặp token")
    public ResponseEntity<ApiResponse<TokenResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        return ResponseEntity.ok(ApiResponse.ok(authService.login(request)));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Làm mới access token", description = "Dùng refresh token hợp lệ để cấp lại access token mới")
    public ResponseEntity<ApiResponse<TokenResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request) {

        return ResponseEntity.ok(ApiResponse.ok(authService.refreshToken(request)));
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
        return ResponseEntity.ok(ApiResponse.ok(authService.getCurrentUser(userId)));
    }

    @GetMapping("/me/permissions")
    @Operation(summary = "Quyền hạn của tôi", description = "Lấy danh sách các quyền và phạm vi dữ liệu tối đa của tài khoản đang đăng nhập")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<List<EffectivePermissionResponse>>> getMyPermissions(Authentication authentication) {
        UUID userId = SecurityUtils.currentUserId(authentication);
        return ResponseEntity.ok(ApiResponse.ok(userRoleService.getEffectivePermissions(userId)));
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
