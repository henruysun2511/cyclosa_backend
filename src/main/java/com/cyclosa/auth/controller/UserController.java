package com.cyclosa.auth.controller;

import com.cyclosa.auth.dto.request.CreateUserRequest;
import com.cyclosa.auth.dto.request.UserFilter;
import com.cyclosa.auth.dto.response.UserResponse;
import com.cyclosa.auth.service.UserService;
import com.cyclosa.common.annotation.RequirePermission;
import com.cyclosa.common.response.ApiResponse;
import com.cyclosa.common.response.PageData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Quản lý tài khoản người dùng và nhân viên nội bộ")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("@perm.has('user.view')")
    @RequirePermission("user.view")
    @Operation(summary = "Tìm kiếm và phân trang danh sách tài khoản", description = "Lấy danh sách người dùng theo từ khóa và trạng thái")
    public ResponseEntity<ApiResponse<PageData<UserResponse>>> getUsers(
            @ModelAttribute UserFilter req) {
        return ResponseEntity.ok(ApiResponse.ok(
                userService.getUsers(req),
                "Lấy danh sách tài khoản thành công"));
    }

    @PostMapping
    @PreAuthorize("@perm.has('user.manage')")
    @RequirePermission("user.manage")
    @Operation(summary = "Tạo tài khoản nhân viên", description = "Quản trị viên tạo tài khoản cho nhân viên. Hệ thống tự sinh mã kích hoạt và gửi email mời đặt mật khẩu.")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody CreateUserRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(
                        userService.createUser(request),
                        "Tạo tài khoản nhân viên thành công. Email kích hoạt đã được gửi."));
    }

    @PostMapping("/{id}/resend-activation")
    @PreAuthorize("@perm.has('user.manage')")
    @RequirePermission("user.manage")
    @Operation(summary = "Gửi lại link kích hoạt", description = "Tạo lại mã kích hoạt mới và gửi lại email mời nhân viên kích hoạt tài khoản")
    public ResponseEntity<ApiResponse<Void>> resendActivation(@PathVariable UUID id) {
        userService.resendActivation(id);
        return ResponseEntity.ok(ApiResponse.noContent("Đã gửi lại email kích hoạt thành công"));
    }

    @PatchMapping("/{id}/lock")
    @PreAuthorize("@perm.has('user.manage')")
    @RequirePermission("user.manage")
    @Operation(summary = "Tạm khóa tài khoản", description = "Khóa tài khoản và thu hồi phiên đăng nhập hiện tại của nhân viên")
    public ResponseEntity<ApiResponse<UserResponse>> lockUser(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(
                userService.lockUser(id),
                "Đã tạm khóa tài khoản thành công"));
    }

    @PatchMapping("/{id}/unlock")
    @PreAuthorize("@perm.has('user.manage')")
    @RequirePermission("user.manage")
    @Operation(summary = "Mở khóa tài khoản", description = "Mở khóa tài khoản để nhân viên tiếp tục đăng nhập")
    public ResponseEntity<ApiResponse<UserResponse>> unlockUser(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(
                userService.unlockUser(id),
                "Đã mở khóa tài khoản thành công"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@perm.has('user.view')")
    @RequirePermission("user.view")
    @Operation(summary = "Xem chi tiết tài khoản", description = "Lấy thông tin chi tiết của tài khoản theo ID")
    public ResponseEntity<ApiResponse<UserResponse>> getUserDetail(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(userService.getUserDetail(id), "Lấy thông tin tài khoản thành công"));
    }
}
