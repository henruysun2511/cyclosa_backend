package com.cyclosa.role.dto.response;

import com.cyclosa.common.enums.DataScope;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleDetailResponse {

    private UUID id;
    private String name;
    private String code;
    private String description;
    private UUID companyId;
    private boolean isSystemRole;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<RolePermissionItem> permissions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RolePermissionItem {
        private UUID permissionId;
        private String permissionCode;
        private String module;
        private String action;
        private String description;
        private DataScope dataScope;
    }
}
