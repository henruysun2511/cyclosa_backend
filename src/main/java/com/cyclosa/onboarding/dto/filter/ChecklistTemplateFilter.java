package com.cyclosa.onboarding.dto.filter;

import com.cyclosa.common.dto.request.BaseFilterRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Bộ lọc tìm kiếm mẫu checklist Onboarding")
public class ChecklistTemplateFilter extends BaseFilterRequest {

    @Schema(description = "Lọc theo vị trí chức danh áp dụng")
    private UUID applicablePositionId;

    @Schema(description = "Lọc theo phòng ban áp dụng")
    private UUID applicableDepartmentId;

    @Schema(description = "Lọc theo trạng thái kích hoạt")
    private Boolean isActive;
}
