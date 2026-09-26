package com.cyclosa.talent.dto.request;

import com.cyclosa.talent.enums.SuccessionReadiness;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu cập nhật thông tin ứng viên kế nhiệm")
public class UpdateSuccessionCandidateRequest {

    @NotNull(message = "Mức độ sẵn sàng không được để trống")
    @Schema(description = "Mức độ sẵn sàng (READY_NOW, READY_1_2_YEARS, READY_3_5_YEARS)")
    private SuccessionReadiness readiness;

    @Schema(description = "Ghi chú đánh giá, kế hoạch đào tạo bồi dưỡng")
    private String note;
}
