package com.cyclosa.performance.dto.request;

import com.cyclosa.common.dto.request.BaseFilterRequest;
import com.cyclosa.performance.enums.EvaluationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Bộ lọc tìm kiếm phiếu đánh giá hiệu suất")
public class EvaluationFilter extends BaseFilterRequest {

    @Schema(description = "ID công ty")
    private UUID companyId;

    @Schema(description = "ID kỳ đánh giá")
    private UUID performanceCycleId;

    @Schema(description = "ID nhân viên được đánh giá")
    private UUID employeeId;

    @Schema(description = "Trạng thái phiếu đánh giá")
    private EvaluationStatus status;
}
