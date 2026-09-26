package com.cyclosa.performance.dto.request;

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
@Schema(description = "Bộ lọc tìm kiếm chỉ số KPI")
public class KpiFilter extends BaseFilterRequest {

    @Schema(description = "ID công ty")
    private UUID companyId;

    @Schema(description = "ID đơn vị tổ chức / phòng ban")
    private UUID organizationalUnitId;

    @Schema(description = "Trạng thái kích hoạt")
    private Boolean isActive;

    @Schema(description = "Từ khóa tìm kiếm (tương đương keyword)")
    private String search;
}
