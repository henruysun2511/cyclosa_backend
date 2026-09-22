package com.cyclosa.payroll.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Yêu cầu khởi chạy tính toán lương")
public class ProcessPayrollRequest {

    @Schema(description = "Danh sách ID nhân viên cần tính (nếu để trống, tính toàn bộ nhân viên công ty)")
    private Set<UUID> employeeIds;
}
