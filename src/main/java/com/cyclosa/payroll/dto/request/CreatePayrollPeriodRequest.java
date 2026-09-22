package com.cyclosa.payroll.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
@Schema(description = "Yêu cầu tạo mới kỳ tính lương")
public class CreatePayrollPeriodRequest {

    @NotBlank(message = "Tên kỳ tính lương không được để trống")
    @Size(max = 150, message = "Tên kỳ tính lương tối đa 150 ký tự")
    @Schema(description = "Tên kỳ lương", example = "Bảng lương Tháng 10/2026")
    private String name;

    @NotBlank(message = "Mã kỳ tính lương không được để trống")
    @Size(max = 50, message = "Mã kỳ tính lương tối đa 50 ký tự")
    @Schema(description = "Mã kỳ lương", example = "PR-2026-10")
    private String code;

    @NotNull(message = "Tháng tính lương không được để trống")
    @Min(value = 1, message = "Tháng từ 1 đến 12")
    @Max(value = 12, message = "Tháng từ 1 đến 12")
    @Schema(description = "Tháng tính lương", example = "10")
    private Integer month;

    @NotNull(message = "Năm tính lương không được để trống")
    @Min(value = 2000, message = "Năm không hợp lệ")
    @Schema(description = "Năm tính lương", example = "2026")
    private Integer year;

    @NotNull(message = "Ngày bắt đầu kỳ không được để trống")
    @Schema(description = "Ngày bắt đầu tính công/lương", example = "2026-10-01")
    private LocalDate startDate;

    @NotNull(message = "Ngày kết thúc kỳ không được để trống")
    @Schema(description = "Ngày kết thúc kỳ", example = "2026-10-31")
    private LocalDate endDate;

    @Schema(description = "Ngày chi trả dự kiến", example = "2026-11-05")
    private LocalDate payDate;

    @Schema(description = "Số ngày công chuẩn của tháng", example = "22.00")
    @Builder.Default
    private BigDecimal standardWorkDays = BigDecimal.valueOf(22.00);
}
