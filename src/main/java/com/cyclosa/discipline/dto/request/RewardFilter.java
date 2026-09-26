package com.cyclosa.discipline.dto.request;

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
@Schema(description = "Bộ lọc tìm kiếm quyết định khen thưởng")
public class RewardFilter extends BaseFilterRequest {

    @Schema(description = "Lọc theo công ty")
    private UUID companyId;

    @Schema(description = "Lọc theo nhân viên được khen thưởng")
    private UUID employeeId;

    @Schema(description = "Từ khóa tìm kiếm (tương đương keyword trong BaseFilterRequest)")
    private String search;
}
