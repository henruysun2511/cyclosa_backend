package com.cyclosa.talent.dto.response;

import com.cyclosa.common.dto.summary.OrgUnitSummary;
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
@Schema(description = "Thống kê xu hướng thăng tiến và luân chuyển theo phòng ban")
public class DepartmentCareerTrendResponse {

    @Schema(description = "Thông tin phòng ban")
    private OrgUnitSummary department;

    @Schema(description = "Tổng số lượt thăng tiến / luân chuyển đã ghi nhận")
    private long totalMovements;

    @Schema(description = "Thời gian thăng tiến trung bình trong phòng ban (năm)")
    private BigDecimal averageTenureBeforePromotion;

    @Schema(description = "Các cặp vị trí có tần suất thăng tiến cao nhất")
    @Builder.Default
    private List<PopularMoveResponse> topPromotions = new ArrayList<>();

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Thông tin cặp vị trí thăng tiến phổ biến")
    public static class PopularMoveResponse {
        private PositionSummary fromPosition;
        private PositionSummary toPosition;
        private long count;
        private BigDecimal averageYears;
    }
}
