package com.cyclosa.payroll.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
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
@Schema(description = "Yêu cầu cập nhật thông tin kỳ lương")
public class UpdatePayrollPeriodRequest {

    @Size(max = 150, message = "Tên kỳ tính lương tối đa 150 ký tự")
    @Schema(description = "Tên kỳ lương", example = "Bảng lương Tháng 10/2026 (Chính thức)")
    private String name;

    @Schema(description = "Ngày chi trả dự kiến", example = "2026-11-05")
    private LocalDate payDate;

    @Schema(description = "Số ngày công chuẩn của tháng", example = "22.00")
    private BigDecimal standardWorkDays;
}
