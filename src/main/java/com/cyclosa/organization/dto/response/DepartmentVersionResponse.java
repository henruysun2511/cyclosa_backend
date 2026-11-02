package com.cyclosa.organization.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin chi tiết một phiên bản cơ cấu phòng ban")
public class DepartmentVersionResponse {

    @Schema(description = "ID phiên bản", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID id;

    @Schema(description = "ID phòng ban")
    private UUID departmentId;

    @Schema(description = "Ngày hiệu lực của phiên bản", example = "2026-01-01")
    private LocalDate effectiveFrom;

    @Schema(description = "Tên phòng ban trong phiên bản này", example = "Phòng Phát triển Phần mềm")
    private String name;

    @Schema(description = "ID phòng ban cấp trên trực tiếp")
    private UUID parentDepartmentId;

    @Schema(description = "Tên phòng ban cấp trên trực tiếp")
    private String parentDepartmentName;

    @Schema(description = "ID trung tâm chi phí hạch toán")
    private UUID costCenterId;

    @Schema(description = "Tên trung tâm chi phí")
    private String costCenterName;

    @Schema(description = "ID nhân viên trưởng bộ phận")
    private UUID managerEmployeeId;

    @Schema(description = "Kế thừa quyền xuống cấp dưới")
    private Boolean isPermissionInherited;

    @Schema(description = "Thời gian tạo bản ghi")
    private LocalDateTime createdAt;
}

