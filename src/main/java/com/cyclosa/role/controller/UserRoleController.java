package com.cyclosa.role.controller;

import com.cyclosa.common.annotation.RequirePermission;
import com.cyclosa.role.dto.request.AssignUserRolesRequest;
import com.cyclosa.role.dto.response.EffectivePermissionResponse;
import com.cyclosa.role.service.UserRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "02. Roles", description = "Gán vai trò và tra cứu quyền hạn người dùng")
public class UserRoleController {

    private final UserRoleService userRoleService;

    @PutMapping("/{userId}/roles")
    @PreAuthorize("@perm.has('user.assign_role')")
    @RequirePermission("user.assign_role")
    @Operation(summary = "Gán danh sách vai trò cho một người dùng")
    public ResponseEntity<Void> assignRolesToUser(
            @PathVariable UUID userId,
            @Valid @RequestBody AssignUserRolesRequest req
    ) {
        userRoleService.assignRolesToUser(userId, req);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{userId}/effective-permissions")
    @PreAuthorize("@perm.has('role.view')")
    @RequirePermission("role.view")
    @Operation(summary = "Lấy danh sách toàn bộ quyền thực tế kèm DataScope cao nhất của người dùng")
    public ResponseEntity<List<EffectivePermissionResponse>> getEffectivePermissions(@PathVariable UUID userId) {
        return ResponseEntity.ok(userRoleService.getEffectivePermissions(userId));
    }
}
