package com.cyclosa.authorization.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin quyền chức năng")
public class PermissionResponse {

    @Schema(description = "ID quyền chức năng")
    private UUID id;

    @Schema(description = "Tài nguyên quản trị (EMPLOYEE, LEAVE, PAYROLL, ROLE...)", example = "EMPLOYEE")
    private String resource;

    @Schema(description = "Hành động cho phép (VIEW, CREATE, UPDATE, DELETE, APPROVE...)", example = "VIEW")
    private String action;

    @Schema(description = "Mã quyền chuẩn", example = "employee.view")
    private String code;

    @Schema(description = "Mô tả chi tiết quyền", example = "Xem thông tin hồ sơ nhân viên")
    private String description;
}

