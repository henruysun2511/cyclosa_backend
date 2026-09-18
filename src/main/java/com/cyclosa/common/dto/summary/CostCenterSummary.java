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
@Schema(description = "Thông tin tóm tắt trung tâm chi phí (Cost Center)")
public class CostCenterSummary {

    @Schema(description = "ID trung tâm chi phí")
    private UUID id;

    @Schema(description = "Mã trung tâm chi phí", example = "CC_TECH_01")
    private String code;

    @Schema(description = "Tên trung tâm chi phí", example = "Trung tâm Chi phí Nghiên cứu & Phát triển")
    private String name;
}
