package com.cyclosa.talent.dto.request;

import com.cyclosa.talent.enums.SuccessionReadiness;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu thêm ứng viên kế nhiệm vào vị trí trọng yếu")
public class AddSuccessionCandidateRequest {

    @NotNull(message = "ID nhân viên ứng viên không được để trống")
    @Schema(description = "ID nhân viên được quy hoạch làm ứng viên kế nhiệm")
    private UUID employeeId;

    @NotNull(message = "Mức độ sẵn sàng không được để trống")
    @Schema(description = "Mức độ sẵn sàng (READY_NOW, READY_1_2_YEARS, READY_3_5_YEARS)")
    private SuccessionReadiness readiness;

    @Schema(description = "Ghi chú đánh giá, kế hoạch đào tạo bồi dưỡng")
    private String note;
}
