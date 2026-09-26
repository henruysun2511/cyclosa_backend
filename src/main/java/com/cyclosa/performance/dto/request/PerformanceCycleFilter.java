package com.cyclosa.performance.dto.request;

import com.cyclosa.common.dto.request.BaseFilterRequest;
import com.cyclosa.performance.enums.PerformanceCycleStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Bộ lọc tìm kiếm chu kỳ đánh giá hiệu suất")
public class PerformanceCycleFilter extends BaseFilterRequest {

    @Schema(description = "ID công ty")
    private UUID companyId;

    @Schema(description = "Trạng thái chu kỳ")
    private PerformanceCycleStatus status;

    @Schema(description = "Từ khóa tìm kiếm (tương đương keyword)")
    private String search;
}
