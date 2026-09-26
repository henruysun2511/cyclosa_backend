package com.cyclosa.talent.dto.request;

import com.cyclosa.talent.enums.SuccessionRisk;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu cập nhật kế hoạch kế nhiệm")
public class UpdateSuccessionPlanRequest {

    @NotNull(message = "Mức độ rủi ro không được để trống")
    @Schema(description = "Mức độ rủi ro thiếu hụt nhân sự kế nhiệm")
    private SuccessionRisk riskLevel;

    @Schema(description = "Ngày định kỳ rà soát kế hoạch kế nhiệm")
    private LocalDate reviewDate;
}
