package com.cyclosa.leave.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Yêu cầu tính toán lại quỹ phép năm")
public class RecalculateLeaveBalanceRequest {

    @NotNull(message = "Năm tính quỹ phép không được để trống")
    @Schema(description = "Năm cần tính toán lại", example = "2026")
    private Integer year;

    @Schema(description = "Tập ID nhân viên cần tính (nếu để trống sẽ tính cho toàn bộ nhân sự công ty)")
    private Set<UUID> employeeIds;
}
