package com.cyclosa.authorization.dto.response;

import com.cyclosa.common.enums.DataScope;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin quyền được gán cho vai trò kèm phạm vi dữ liệu")
public class RolePermissionResponse {

    @Schema(description = "ID bản ghi phân quyền")
    private UUID id;

    @Schema(description = "ID quyền chức năng")
    private UUID permissionId;

    @Schema(description = "Mã quyền", example = "employee.view")
    private String permissionCode;

    @Schema(description = "Phân hệ / Tài nguyên", example = "EMPLOYEE")
    private String resource;

    @Schema(description = "Hành động", example = "VIEW")
    private String action;

    @Schema(description = "Mô tả quyền")
    private String description;

    @Schema(description = "Phạm vi dữ liệu áp dụng", example = "DEPARTMENT")
    private DataScope dataScope;
}

