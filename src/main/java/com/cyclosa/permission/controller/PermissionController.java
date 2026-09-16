package com.cyclosa.permission.controller;

import com.cyclosa.common.annotation.RequirePermission;
import com.cyclosa.common.response.ApiResponse;
import com.cyclosa.permission.dto.response.PermissionResponse;
import com.cyclosa.permission.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
@Tag(name = "Permissions", description = "Danh mục quyền hạn trong hệ thống")
public class PermissionController {

    private final PermissionService permissionService;

    @GetMapping
    @PreAuthorize("@perm.has('role.view')")
    @RequirePermission("role.view")
    @Operation(summary = "Lấy toàn bộ danh mục quyền hệ thống")
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> getAllPermissions() {
        return ResponseEntity.ok(ApiResponse.ok(permissionService.getAllPermissions(), "Lấy danh mục quyền hạn thành công"));
    }

    @GetMapping("/grouped")
    @PreAuthorize("@perm.has('role.view')")
    @RequirePermission("role.view")
    @Operation(summary = "Lấy danh mục quyền gom nhóm theo module")
    public ResponseEntity<ApiResponse<Map<String, List<PermissionResponse>>>> getPermissionsGrouped() {
        return ResponseEntity.ok(ApiResponse.ok(permissionService.getPermissionsGroupedByModule(), "Lấy danh mục quyền hạn theo nhóm thành công"));
    }
}
