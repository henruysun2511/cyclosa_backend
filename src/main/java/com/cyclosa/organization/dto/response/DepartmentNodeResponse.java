package com.cyclosa.organization.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Node phòng ban trong cây sơ đồ tổ chức (Org Tree)")
public class DepartmentNodeResponse {

    @Schema(description = "ID phòng ban", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID id;

    @Schema(description = "Mã phòng ban", example = "DEPT_TECH_01")
    private String code;

    @Schema(description = "Tên phòng ban", example = "Phòng Phát triển Phần mềm")
    private String name;

    @Schema(description = "ID phòng ban cha")
    private UUID parentDepartmentId;

    @Schema(description = "ID trung tâm chi phí")
    private UUID costCenterId;

    @Schema(description = "Tên trung tâm chi phí")
    private String costCenterName;

    @Schema(description = "ID trưởng phòng")
    private UUID managerEmployeeId;

    @Schema(description = "Kế thừa quyền xuống cấp dưới")
    private Boolean isPermissionInherited;

    @Schema(description = "Ngày hiệu lực áp dụng")
    private LocalDate effectiveFrom;

    @Schema(description = "Số lượng nhân viên trực thuộc phòng ban", example = "25")
    @Builder.Default
    private Integer memberCount = 0;

    @Schema(description = "Danh sách các phòng ban trực thuộc cấp con")
    @Builder.Default
    private List<DepartmentNodeResponse> children = new ArrayList<>();
}

