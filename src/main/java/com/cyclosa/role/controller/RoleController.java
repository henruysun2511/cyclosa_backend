package com.cyclosa.role.controller;

import com.cyclosa.common.annotation.RequirePermission;
import com.cyclosa.role.dto.request.AssignRolePermissionsRequest;
import com.cyclosa.role.dto.request.RoleRequest;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
@Tag(name = "02. Roles", description = "Quản lý vai trò và phân quyền trong hệ thống")
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    @PreAuthorize("@perm.has('role.view')")
    @RequirePermission("role.view")
    @Operation(summary = "Lấy danh sách vai trò (hệ thống và của công ty)")
    public ResponseEntity<List<RoleResponse>> getRoles(
            @RequestParam(required = false) UUID companyId
    ) {
        return ResponseEntity.ok(roleService.getRoles(companyId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@perm.has('role.view')")
    @RequirePermission("role.view")
    @Operation(summary = "Lấy thông tin chi tiết vai trò kèm danh sách quyền hạn")
    public ResponseEntity<RoleDetailResponse> getRoleById(@PathVariable UUID id) {
        return ResponseEntity.ok(roleService.getRoleById(id));
    }

    @PostMapping
    @PreAuthorize("@perm.has('role.create')")
    @RequirePermission("role.create")
    @Operation(summary = "Tạo vai trò tùy biến mới cho công ty")
    public ResponseEntity<RoleResponse> createRole(@Valid @RequestBody RoleRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roleService.createRole(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@perm.has('role.update')")
    @RequirePermission("role.update")
    @Operation(summary = "Cập nhật thông tin vai trò")
    public ResponseEntity<RoleResponse> updateRole(
            @PathVariable UUID id,
            @Valid @RequestBody RoleRequest req
    ) {
        return ResponseEntity.ok(roleService.updateRole(id, req));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@perm.has('role.delete')")
    @RequirePermission("role.delete")
    @Operation(summary = "Xóa vai trò tùy biến (không cho phép xóa vai trò hệ thống)")
    public ResponseEntity<Void> deleteRole(@PathVariable UUID id) {
        roleService.deleteRole(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/permissions")
    @PreAuthorize("@perm.has('role.assign')")
    @RequirePermission("role.assign")
    @Operation(summary = "Gán danh sách quyền và phạm vi dữ liệu (data_scope) cho vai trò")
    public ResponseEntity<RoleDetailResponse> assignPermissions(
            @PathVariable UUID id,
            @Valid @RequestBody AssignRolePermissionsRequest req
    ) {
        return ResponseEntity.ok(roleService.assignPermissions(id, req));
    }
}
