package com.cyclosa.talent.dto.request;

import com.cyclosa.talent.enums.SuccessionRisk;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu lập kế hoạch kế nhiệm")
public class CreateSuccessionPlanRequest {

    @NotNull(message = "Vị trí trọng yếu không được để trống")
    @Schema(description = "ID vị trí trọng yếu cần lập kế hoạch kế nhiệm")
    private UUID positionId;

    @NotNull(message = "Mức độ rủi ro không được để trống")
    @Schema(description = "Mức độ rủi ro thiếu hụt nhân sự kế nhiệm (LOW, MEDIUM, HIGH, CRITICAL)")
    private SuccessionRisk riskLevel;

    @Schema(description = "Ngày định kỳ rà soát kế hoạch kế nhiệm")
    private LocalDate reviewDate;
}
