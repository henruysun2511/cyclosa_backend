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
@Schema(description = "Bộ lọc tìm kiếm mục tiêu hiệu suất")
public class GoalFilter extends BaseFilterRequest {

    @Schema(description = "ID nhân viên")
    private UUID employeeId;

    @Schema(description = "ID kỳ đánh giá")
    private UUID performanceCycleId;
}
