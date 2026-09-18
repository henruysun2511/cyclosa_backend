package com.cyclosa.common.dto.summary;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin tóm tắt đơn vị / phòng ban")
public class OrgUnitSummary {

    @Schema(description = "ID đơn vị tổ chức")
    private UUID id;

    @Schema(description = "Mã phòng ban / đơn vị", example = "DEPT_TECH")
    private String code;

    @Schema(description = "Tên phòng ban / đơn vị", example = "Phòng Công nghệ Thông tin")
    private String name;

    @Schema(description = "Loại đơn vị (DIVISION, DEPARTMENT, TEAM, SECTION)", example = "DEPARTMENT")
    private String unitType;
}
