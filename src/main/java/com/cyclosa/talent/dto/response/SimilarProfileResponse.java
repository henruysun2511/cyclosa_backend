package com.cyclosa.talent.dto.response;

import com.cyclosa.common.dto.summary.EmployeeSummary;
import com.cyclosa.common.dto.summary.PositionSummary;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin hồ sơ nhân sự có xuất phát điểm hoặc đặc tính tương đồng")
public class SimilarProfileResponse {

    @Schema(description = "Thông tin tóm tắt nhân sự")
    private EmployeeSummary employee;

    @Schema(description = "Vị trí hiện tại của nhân sự đó")
    private PositionSummary currentPosition;

    @Schema(description = "Thâm niên công tác (năm)")
    private BigDecimal yearsOfTenure;

    @Schema(description = "Độ tương đồng (%) so với hồ sơ đang xét")
    private double similarityScore;
}
