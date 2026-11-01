package com.cyclosa.auth.controller;

import com.cyclosa.auth.dto.request.CreateUserRequest;
import com.cyclosa.auth.dto.request.UserFilterRequest;
import com.cyclosa.auth.dto.response.UserResponse;
import com.cyclosa.auth.service.UserService;
import com.cyclosa.common.response.ApiResponse;
import com.cyclosa.common.response.PageData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "0. Quản lý Tài khoản (User Management)", description = "Các API dành cho Quản trị viên để tạo, xem, khóa/mở khóa tài khoản")
public class UserController {

    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Tạo tài khoản nhân viên mới",
               description = "Khởi tạo tài khoản ở trạng thái PENDING_ACTIVATION, sinh mã 48 giờ và gửi link qua email")
    public ApiResponse<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserResponse response = userService.createUser(request);
        return ApiResponse.created(response);
    }

    @GetMapping
    @Operation(summary = "Lấy danh sách tài khoản người dùng",
               description = "Tìm kiếm theo từ khóa, lọc theo trạng thái (PENDING_ACTIVATION, ACTIVE, LOCKED) và phân trang")
    public ApiResponse<PageData<UserResponse>> getUsers(@ParameterObject @Valid UserFilterRequest filter) {
        PageData<UserResponse> response = userService.getUsers(filter);
        return ApiResponse.ok(response);
    }

    @PostMapping("/{userId}/resend-activation")
    @Operation(summary = "Gửi lại email kích hoạt tài khoản",
               description = "Chỉ áp dụng khi tài khoản đang ở trạng thái PENDING_ACTIVATION (sinh mã mới 48 giờ)")
    public ApiResponse<Void> resendActivation(
            @Parameter(description = "ID người dùng", required = true)
            @PathVariable UUID userId) {
        userService.resendActivation(userId);
        return ApiResponse.ok(null);
    }

    @PostMapping("/{userId}/lock")
    @Operation(summary = "Khóa tài khoản người dùng",
               description = "Chuyển trạng thái sang LOCKED và hủy phiên đăng nhập tức thì")
    public ApiResponse<Void> lockUser(
            @Parameter(description = "ID người dùng", required = true)
            @PathVariable UUID userId) {
        userService.lockUser(userId);
        return ApiResponse.ok(null);
    }

    @PostMapping("/{userId}/unlock")
    @Operation(summary = "Mở khóa tài khoản người dùng")
    public ApiResponse<Void> unlockUser(
            @Parameter(description = "ID người dùng", required = true)
            @PathVariable UUID userId) {
        userService.unlockUser(userId);
        return ApiResponse.ok(null);
    }
}

