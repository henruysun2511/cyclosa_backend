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
@Schema(description = "Thông tin tóm tắt vùng / khu vực địa lý")
public class RegionSummary {

    @Schema(description = "ID vùng")
    private UUID id;

    @Schema(description = "Mã vùng", example = "NORTH")
    private String code;

    @Schema(description = "Tên vùng", example = "Miền Bắc")
    private String name;
}
