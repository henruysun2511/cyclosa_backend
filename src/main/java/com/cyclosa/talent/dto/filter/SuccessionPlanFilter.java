package com.cyclosa.talent.dto.filter;

import com.cyclosa.common.dto.request.BaseFilterRequest;
import com.cyclosa.talent.enums.SuccessionRisk;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Bộ lọc danh sách kế hoạch kế nhiệm")
public class SuccessionPlanFilter extends BaseFilterRequest {

    @Schema(description = "Lọc theo công ty")
    private UUID companyId;

    @Schema(description = "Lọc theo vị trí trọng yếu")
    private UUID positionId;

    @Schema(description = "Lọc theo mức độ rủi ro")
    private SuccessionRisk riskLevel;
}
