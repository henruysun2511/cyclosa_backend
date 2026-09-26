package com.cyclosa.talent.dto.response;

import com.cyclosa.common.dto.summary.PositionSummary;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin kịch bản lộ trình mô phỏng đã lưu")
public class CareerSimulationSavedPathResponse {

    @Schema(description = "ID kịch bản đã lưu")
    private UUID id;

    @Schema(description = "ID công ty")
    private UUID companyId;

    @Schema(description = "ID nhân viên")
    private UUID employeeId;

    @Schema(description = "ID vị trí mục tiêu")
    private UUID targetPositionId;

    @Schema(description = "Thông tin tóm tắt vị trí mục tiêu")
    private PositionSummary targetPosition;

    @Schema(description = "Dữ liệu JSON snapshot kịch bản lộ trình gợi ý")
    private String suggestedPath;

    @Schema(description = "Thời điểm lưu kịch bản")
    private LocalDateTime savedAt;
}
