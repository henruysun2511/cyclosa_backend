package com.cyclosa.talent.dto.response;

import com.cyclosa.common.dto.summary.PositionSummary;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin lộ trình thăng tiến chuẩn")
public class CareerPathResponse {

    @Schema(description = "ID lộ trình")
    private UUID id;

    @Schema(description = "ID công ty")
    private UUID companyId;

    @Schema(description = "ID vị trí xuất phát")
    private UUID fromPositionId;

    @Schema(description = "Thông tin tóm tắt vị trí xuất phát")
    private PositionSummary fromPosition;

    @Schema(description = "ID vị trí đích")
    private UUID toPositionId;

    @Schema(description = "Thông tin tóm tắt vị trí đích")
    private PositionSummary toPosition;

    @Schema(description = "Mô tả tiêu chuẩn, điều kiện thăng tiến")
    private String description;

    @Schema(description = "Số năm kinh nghiệm tối thiểu yêu cầu")
    private BigDecimal minYearsRequired;

    @Schema(description = "Thời điểm tạo")
    private LocalDateTime createdAt;

    @Schema(description = "Thời điểm cập nhật")
    private LocalDateTime updatedAt;
}
