package com.cyclosa.talent.dto.filter;

import com.cyclosa.common.dto.request.BaseFilterRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Bộ lọc danh sách lộ trình thăng tiến chuẩn")
public class CareerPathFilter extends BaseFilterRequest {

    @Schema(description = "Lọc theo công ty")
    private UUID companyId;

    @Schema(description = "Lọc theo vị trí xuất phát")
    private UUID fromPositionId;

    @Schema(description = "Lọc theo vị trí đích")
    private UUID toPositionId;
}
