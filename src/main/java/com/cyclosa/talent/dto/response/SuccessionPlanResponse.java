package com.cyclosa.talent.dto.response;

import com.cyclosa.common.dto.summary.PositionSummary;
import com.cyclosa.talent.enums.SuccessionRisk;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin tóm tắt kế hoạch kế nhiệm")
public class SuccessionPlanResponse {

    @Schema(description = "ID kế hoạch")
    private UUID id;

    @Schema(description = "ID công ty")
    private UUID companyId;

    @Schema(description = "ID vị trí trọng yếu")
    private UUID positionId;

    @Schema(description = "Thông tin tóm tắt vị trí")
    private PositionSummary position;

    @Schema(description = "Mức độ rủi ro thiếu hụt nhân sự")
    private SuccessionRisk riskLevel;

    @Schema(description = "Ngày định kỳ rà soát")
    private LocalDate reviewDate;

    @Schema(description = "Số lượng ứng viên kế nhiệm hiện có")
    private int candidateCount;

    @Schema(description = "Thời điểm tạo")
    private LocalDateTime createdAt;

    @Schema(description = "Thời điểm cập nhật")
    private LocalDateTime updatedAt;
}
