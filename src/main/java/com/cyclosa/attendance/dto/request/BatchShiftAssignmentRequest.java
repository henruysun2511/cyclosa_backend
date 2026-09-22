package com.cyclosa.attendance.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu phân ca hàng loạt cho nhân viên")
public class BatchShiftAssignmentRequest {

    @NotEmpty(message = "Danh sách ID nhân viên không được để trống")
    @Schema(description = "Danh sách ID nhân sự")
    private Set<UUID> employeeIds;

    @NotNull(message = "ID ca làm việc không được để trống")
    @Schema(description = "ID ca làm việc")
    private UUID shiftId;

    @NotNull(message = "Ngày bắt đầu áp dụng không được để trống")
    @Schema(description = "Ngày bắt đầu", example = "2026-10-01")
    private LocalDate fromDate;

    @NotNull(message = "Ngày kết thúc áp dụng không được để trống")
    @Schema(description = "Ngày kết thúc", example = "2026-10-31")
    private LocalDate toDate;

    @Schema(description = "Danh sách các ngày trong tuần áp dụng (để trống nếu áp dụng tất cả)", example = "[\"MONDAY\", \"TUESDAY\", \"WEDNESDAY\", \"THURSDAY\", \"FRIDAY\"]")
    private List<DayOfWeek> daysOfWeek;

    @Schema(description = "Ghi chú phân ca", example = "Phân ca hành chính tháng 10")
    private String note;
}
