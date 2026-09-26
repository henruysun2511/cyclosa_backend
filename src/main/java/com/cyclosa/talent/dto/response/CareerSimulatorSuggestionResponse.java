package com.cyclosa.talent.dto.response;

import com.cyclosa.common.dto.summary.PositionSummary;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Gợi ý lộ trình thăng tiến dựa trên phân tích hồ sơ tương tự")
public class CareerSimulatorSuggestionResponse {

    @Schema(description = "Vị trí mục tiêu tiềm năng")
    private PositionSummary targetPosition;

    @Schema(description = "Tỷ lệ tương đồng (%) của các hồ sơ trước đây đã thăng tiến thành công")
    private double matchPercentage;

    @Schema(description = "Số năm trung bình để đạt được vị trí này từ vị trí hiện tại")
    private BigDecimal averageYearsToPromote;

    @Schema(description = "Số lượng hồ sơ lịch sử dùng làm căn cứ thống kê")
    private int sampleProfileCount;

    @Schema(description = "Các bước chuyển tiếp trung gian")
    @Builder.Default
    private List<CareerPathResponse> progressionSteps = new ArrayList<>();

    @Schema(description = "Gợi ý các kỹ năng hoặc mục tiêu cần hoàn thành")
    private String recommendation;
}
