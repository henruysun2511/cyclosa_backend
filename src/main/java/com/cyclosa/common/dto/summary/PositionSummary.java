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
@Schema(description = "Thông tin tóm tắt chức danh / vị trí công việc")
public class PositionSummary {

    @Schema(description = "ID chức danh")
    private UUID id;

    @Schema(description = "Mã chức danh", example = "POS_DEV_SR")
    private String code;

    @Schema(description = "Tên chức danh", example = "Kỹ sư Phần mềm Cao cấp")
    private String name;
}
