package com.cyclosa.talent.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu thêm nhân sự vào kho nhân tài nội bộ (HiPo, Key Talent)")
public class AddTalentPoolRequest {

    @NotNull(message = "ID nhân viên không được để trống")
    @Schema(description = "ID nhân viên được đưa vào kho nhân tài")
    private UUID employeeId;

    @NotBlank(message = "Thẻ phân loại không được để trống")
    @Schema(description = "Thẻ phân loại (VD: HiPo, Leadership Potential, Key Talent, Tech Expert)")
    private String tag;

    @Schema(description = "Ghi chú lý do, tiềm năng phát triển")
    private String note;
}
