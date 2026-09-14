package com.cyclosa.role.dto.request;

import com.cyclosa.common.enums.DataScope;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignRolePermissionsRequest {

    @NotEmpty(message = "Danh sách quyền không được để trống")
    @Valid
    private List<PermissionAssignment> permissions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PermissionAssignment {

        @NotNull(message = "ID quyền không được để trống")
        private UUID permissionId;

        @NotNull(message = "Phạm vi dữ liệu (dataScope) không được để trống")
        private DataScope dataScope;
    }
}
