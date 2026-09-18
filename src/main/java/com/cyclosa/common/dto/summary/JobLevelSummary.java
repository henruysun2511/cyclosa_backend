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
@Schema(description = "Thông tin tóm tắt cấp bậc công việc")
public class JobLevelSummary {

    @Schema(description = "ID cấp bậc")
    private UUID id;

    @Schema(description = "Tên cấp bậc", example = "Level 4 - Senior")
    private String name;

    @Schema(description = "Thứ tự cấp bậc", example = "4")
    private Integer rankOrder;
}
