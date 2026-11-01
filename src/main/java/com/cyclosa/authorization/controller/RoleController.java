package com.cyclosa.authorization.controller;

import com.cyclosa.common.response.ApiResponse;
import com.cyclosa.common.response.PageData;
import com.cyclosa.authorization.dto.request.AssignRolePermissionsRequest;
import com.cyclosa.authorization.dto.request.CreateRoleRequest;
import com.cyclosa.authorization.dto.request.RoleDocumentPermissionRequest;
import com.cyclosa.authorization.dto.request.RoleFilterRequest;
import com.cyclosa.authorization.dto.request.UpdateRoleRequest;
import com.cyclosa.authorization.dto.response.RolePermissionResponse;
import com.cyclosa.authorization.dto.response.RoleResponse;
import com.cyclosa.authorization.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
@Tag(name = "03. Phân quyền - Quản lý Vai trò", description = "APIs quản lý vai trò và thiết lập ma trận phân quyền")
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    @Operation(summary = "Lấy danh sách vai trò có phân trang và bộ lọc")
    @PreAuthorize("@perm.has('role.view')")
    public ApiResponse<PageData<RoleResponse>> getRoles(@ParameterObject @Valid RoleFilterRequest filter) {
        return ApiResponse.ok(roleService.getRoles(filter));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Tạo mới vai trò tùy chỉnh")
    @PreAuthorize("@perm.has('role.manage')")
    public ApiResponse<RoleResponse> createRole(@Valid @RequestBody CreateRoleRequest request) {
        return ApiResponse.created(roleService.createRole(request));
    }

    @PutMapping("/{roleId}")
    @Operation(summary = "Cập nhật tên và mô tả vai trò tùy chỉnh")
    @PreAuthorize("@perm.has('role.manage')")
    public ApiResponse<RoleResponse> updateRole(
            @Parameter(description = "ID vai trò", required = true)
            @PathVariable UUID roleId,
            @Valid @RequestBody UpdateRoleRequest request) {
        return ApiResponse.ok(roleService.updateRole(roleId, request));
    }

    @DeleteMapping("/{roleId}")
    @Operation(summary = "Xóa mềm vai trò tùy chỉnh (chỉ khi không có người dùng nắm giữ)")
    @PreAuthorize("@perm.has('role.manage')")
    public ApiResponse<Void> deleteRole(
            @Parameter(description = "ID vai trò", required = true)
            @PathVariable UUID roleId) {
        roleService.deleteRole(roleId);
        return ApiResponse.ok(null);
    }

    @GetMapping("/{roleId}/permissions")
    @Operation(summary = "Lấy danh sách quyền chức năng và phạm vi dữ liệu của vai trò")
    @PreAuthorize("@perm.has('role.view')")
    public ApiResponse<List<RolePermissionResponse>> getRolePermissions(
            @Parameter(description = "ID vai trò", required = true)
            @PathVariable UUID roleId) {
        return ApiResponse.ok(roleService.getRolePermissions(roleId));
    }

    @PutMapping("/{roleId}/permissions")
    @Operation(summary = "Thiết lập ma trận quyền kèm data_scope cho vai trò")
    @PreAuthorize("@perm.has('role.manage')")
    public ApiResponse<List<RolePermissionResponse>> assignPermissions(
            @Parameter(description = "ID vai trò", required = true)
            @PathVariable UUID roleId,
            @Valid @RequestBody AssignRolePermissionsRequest request) {
        return ApiResponse.ok(roleService.assignPermissions(roleId, request));
    }

    @PutMapping("/{roleId}/document-type-permissions")
    @Operation(summary = "Cấu hình quyền thao tác tài liệu số cho vai trò")
    @PreAuthorize("@perm.has('role.manage')")
    public ApiResponse<Void> configureDocumentPermissions(
            @Parameter(description = "ID vai trò", required = true)
            @PathVariable UUID roleId,
            @Valid @RequestBody RoleDocumentPermissionRequest request) {
        roleService.configureDocumentPermissions(roleId, request);
        return ApiResponse.ok(null);
    }
}
