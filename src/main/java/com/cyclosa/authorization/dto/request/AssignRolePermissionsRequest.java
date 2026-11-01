package com.cyclosa.authorization.dto.request;

import com.cyclosa.common.enums.DataScope;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Schema(description = "Yêu cầu cấu hình danh sách quyền và phạm vi dữ liệu cho vai trò")
public class AssignRolePermissionsRequest {

    @Valid
    @NotNull(message = "Danh sách quyền không được null")
    @Schema(description = "Danh sách quyền chức năng và phạm vi tương ứng")
    private List<PermissionScopeItem> permissions = new ArrayList<>();

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Cặp quyền chức năng và phạm vi dữ liệu")
    public static class PermissionScopeItem {

        @NotNull(message = "Mã ID quyền không được để trống")
        @Schema(description = "ID của quyền trong bảng permissions", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        private UUID permissionId;

        @NotNull(message = "Phạm vi dữ liệu không được để trống")
        @Schema(description = "Phạm vi dữ liệu (OWN, TEAM, DEPARTMENT, COMPANY, ALL)", example = "DEPARTMENT")
        private DataScope dataScope;
    }
}

