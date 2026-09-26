package com.cyclosa.talent.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu lưu kịch bản lộ trình mô phỏng sự nghiệp")
public class CareerSimulationSaveRequest {

    @Schema(description = "ID vị trí mục tiêu hướng tới (tùy chọn)")
    private UUID targetPositionId;

    @NotBlank(message = "Dữ liệu gợi ý lộ trình không được để trống")
    @Schema(description = "Nội dung JSON snapshot kịch bản lộ trình và các mốc phát triển")
    private String suggestedPath;
}
