package com.cyclosa.payroll.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu xin tạm ứng tiền lương")
public class CreateSalaryAdvanceRequest {

    @NotNull(message = "Ngày đề xuất tạm ứng không được để trống")
    @Schema(description = "Ngày đề xuất", example = "2026-10-15")
    private LocalDate requestDate;

    @NotNull(message = "Số tiền xin tạm ứng không được để trống")
    @DecimalMin(value = "100000", message = "Số tiền tạm ứng tối thiểu là 100,000 VNĐ")
    @Schema(description = "Số tiền xin ứng", example = "5000000")
    private BigDecimal amount;

    @NotBlank(message = "Lý do tạm ứng không được để trống")
    @Schema(description = "Lý do xin tạm ứng", example = "Tạm ứng chi phí gia đình có việc khẩn cấp")
    private String reason;
}
