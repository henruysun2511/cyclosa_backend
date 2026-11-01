package com.cyclosa.authorization.controller;

import com.cyclosa.common.response.ApiResponse;
import com.cyclosa.authorization.dto.response.PermissionResponse;
import com.cyclosa.authorization.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
@Tag(name = "03. Phân quyền - Quyền chức năng", description = "APIs tra cứu danh mục quyền chức năng hệ thống")
public class PermissionController {

    private final PermissionService permissionService;

    @GetMapping("/grouped")
    @Operation(summary = "Lấy danh mục quyền nhóm theo phân hệ tài nguyên (Resource)")
    @PreAuthorize("@perm.has('role.view')")
    public ApiResponse<Map<String, List<PermissionResponse>>> getPermissionsGrouped() {
        return ApiResponse.ok(permissionService.getPermissionsGroupedByResource());
    }
}
