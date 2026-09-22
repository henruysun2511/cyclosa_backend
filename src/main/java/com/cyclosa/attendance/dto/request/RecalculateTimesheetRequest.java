package com.cyclosa.attendance.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu tính toán lại bảng công tháng")
public class RecalculateTimesheetRequest {

    @NotNull(message = "Tháng không được để trống")
    @Min(value = 1, message = "Tháng từ 1 đến 12")
    @Max(value = 12, message = "Tháng từ 1 đến 12")
    @Schema(description = "Tháng tính công", example = "10")
    private Integer month;

    @NotNull(message = "Năm không được để trống")
    @Min(value = 2000, message = "Năm không hợp lệ")
    @Schema(description = "Năm tính công", example = "2026")
    private Integer year;

    @Schema(description = "Danh sách ID nhân viên cần tính lại (để trống nếu tính toán toàn bộ công ty)")
    private Set<UUID> employeeIds;
}
