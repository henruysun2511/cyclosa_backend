package com.cyclosa.role.controller;

import com.cyclosa.common.annotation.RequirePermission;
import com.cyclosa.common.response.ApiResponse;
import com.cyclosa.common.response.PageData;
import com.cyclosa.role.dto.request.AssignRolePermissionsRequest;
import com.cyclosa.role.dto.request.RoleRequest;
import com.cyclosa.role.dto.request.RoleFilter;
import com.cyclosa.role.dto.response.RoleDetailResponse;
import com.cyclosa.role.dto.response.RoleResponse;
import com.cyclosa.role.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
@Tag(name = "Roles", description = "Quản lý vai trò và phân quyền trong hệ thống")
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    @PreAuthorize("@perm.has('role.view')")
    @RequirePermission("role.view")
    @Operation(summary = "Tìm kiếm và phân trang danh sách vai trò")
    public ResponseEntity<ApiResponse<PageData<RoleResponse>>> getRoles(
            @ModelAttribute RoleFilter req
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                roleService.getRoles(req),
                "Lấy danh sách vai trò thành công"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@perm.has('role.view')")
    @RequirePermission("role.view")
    @Operation(summary = "Lấy thông tin chi tiết vai trò kèm danh sách quyền hạn")
    public ResponseEntity<ApiResponse<RoleDetailResponse>> getRoleById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(roleService.getRoleById(id), "Lấy thông tin vai trò thành công"));
    }

    @PostMapping
    @PreAuthorize("@perm.has('role.create')")
    @RequirePermission("role.create")
    @Operation(summary = "Tạo vai trò tùy biến mới cho công ty")
    public ResponseEntity<ApiResponse<RoleResponse>> createRole(@Valid @RequestBody RoleRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(roleService.createRole(req), "Tạo vai trò thành công"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@perm.has('role.update')")
    @RequirePermission("role.update")
    @Operation(summary = "Cập nhật thông tin vai trò")
    public ResponseEntity<ApiResponse<RoleResponse>> updateRole(
            @PathVariable UUID id,
            @Valid @RequestBody RoleRequest req
    ) {
        return ResponseEntity.ok(ApiResponse.ok(roleService.updateRole(id, req), "Cập nhật vai trò thành công"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@perm.has('role.delete')")
    @RequirePermission("role.delete")
    @Operation(summary = "Xóa vai trò tùy biến (không cho phép xóa vai trò hệ thống)")
    public ResponseEntity<ApiResponse<Void>> deleteRole(@PathVariable UUID id) {
        roleService.deleteRole(id);
        return ResponseEntity.ok(ApiResponse.noContent("Xóa vai trò thành công"));
    }

    @PutMapping("/{id}/permissions")
    @PreAuthorize("@perm.has('role.assign')")
    @RequirePermission("role.assign")
    @Operation(summary = "Gán danh sách quyền và phạm vi dữ liệu (data_scope) cho vai trò")
    public ResponseEntity<ApiResponse<RoleDetailResponse>> assignPermissions(
            @PathVariable UUID id,
            @Valid @RequestBody AssignRolePermissionsRequest req
    ) {
        return ResponseEntity.ok(ApiResponse.ok(roleService.assignPermissions(id, req), "Gán quyền cho vai trò thành công"));
    }
}
