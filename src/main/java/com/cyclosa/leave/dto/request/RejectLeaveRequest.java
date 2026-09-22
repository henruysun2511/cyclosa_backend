package com.cyclosa.leave.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu từ chối đơn xin nghỉ phép")
public class RejectLeaveRequest {

    @NotBlank(message = "Lý do từ chối không được để trống")
    @Schema(description = "Lý do từ chối", example = "Trùng lịch bàn giao dự án quan trọng với khách hàng")
    private String reason;
}
