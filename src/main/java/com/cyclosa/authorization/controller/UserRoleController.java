package com.cyclosa.authorization.controller;

import com.cyclosa.common.response.ApiResponse;
import com.cyclosa.authorization.dto.request.AssignUserRoleRequest;
import com.cyclosa.authorization.dto.response.EffectivePermissionResponse;
import com.cyclosa.authorization.dto.response.UserRoleAssignmentResponse;
import com.cyclosa.authorization.service.UserRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "03. Phân quyền - Gán Vai trò & Quyền Hiệu lực", description = "APIs gán vai trò người dùng theo pháp nhân công ty và tra cứu quyền hiệu lực")
public class UserRoleController {

    private final UserRoleService userRoleService;

    @GetMapping("/api/v1/users/{userId}/role-assignments")
    @Operation(summary = "Lấy danh sách các vai trò được gán cho người dùng")
    @PreAuthorize("@perm.has('role.view')")
    public ApiResponse<List<UserRoleAssignmentResponse>> getUserRoles(
            @Parameter(description = "ID người dùng", required = true)
            @PathVariable UUID userId) {
        return ApiResponse.ok(userRoleService.getUserRoles(userId));
    }

    @PostMapping("/api/v1/users/{userId}/role-assignments")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Gán vai trò cho người dùng tại một công ty cụ thể")
    @PreAuthorize("@perm.has('role.assign')")
    public ApiResponse<UserRoleAssignmentResponse> assignRoleToUser(
            @Parameter(description = "ID người dùng", required = true)
            @PathVariable UUID userId,
            @Valid @RequestBody AssignUserRoleRequest request) {
        return ApiResponse.created(userRoleService.assignRoleToUser(userId, request));
    }

    @DeleteMapping("/api/v1/role-assignments/{assignmentId}")
    @Operation(summary = "Thu hồi vai trò đã gán khỏi người dùng")
    @PreAuthorize("@perm.has('role.assign')")
    public ApiResponse<Void> revokeRoleAssignment(
            @Parameter(description = "ID bản ghi gán vai trò", required = true)
            @PathVariable UUID assignmentId) {
        userRoleService.revokeRoleAssignment(assignmentId);
        return ApiResponse.ok(null);
    }

    @GetMapping("/api/v1/users/{userId}/effective-permissions")
    @Operation(summary = "Lấy ma trận quyền hiệu lực của người dùng tại công ty (sau khi tính max DataScope)")
    @PreAuthorize("@perm.has('role.view')")
    public ApiResponse<List<EffectivePermissionResponse>> getEffectivePermissions(
            @Parameter(description = "ID người dùng", required = true)
            @PathVariable UUID userId,
            @Parameter(description = "ID công ty (để trống nếu toàn hệ thống)")
            @RequestParam(required = false) UUID companyId) {
        return ApiResponse.ok(userRoleService.getEffectivePermissionsResponse(userId, companyId));
    }
}

