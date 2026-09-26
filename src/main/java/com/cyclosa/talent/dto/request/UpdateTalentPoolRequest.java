package com.cyclosa.talent.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu cập nhật hồ sơ kho nhân tài nội bộ")
public class UpdateTalentPoolRequest {

    @NotBlank(message = "Thẻ phân loại không được để trống")
    @Schema(description = "Thẻ phân loại (VD: HiPo, Leadership Potential, Key Talent)")
    private String tag;

    @Schema(description = "Ghi chú lý do, định hướng phát triển")
    private String note;
}
