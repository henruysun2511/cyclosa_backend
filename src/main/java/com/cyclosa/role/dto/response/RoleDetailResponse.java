package com.cyclosa.role.dto.response;

import com.cyclosa.common.dto.summary.CompanySummary;
import com.cyclosa.common.enums.DataScope;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Chi tiết thông tin vai trò và danh sách quyền hạn")
public class RoleDetailResponse {

    private UUID id;
    private String name;
    private String code;
    private String description;

    @Schema(description = "Công ty sở hữu vai trò (null nếu là vai trò hệ thống)")
    private CompanySummary company;

    private boolean isSystemRole;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Schema(description = "Danh sách chi tiết các quyền hạn được gán cho vai trò")
    private List<RolePermissionItem> permissions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Chi tiết quyền hạn được gán")
    public static class RolePermissionItem {
        private UUID permissionId;
        private String permissionCode;
        private String module;
        private String action;
        private String description;
        private DataScope dataScope;
    }
}
