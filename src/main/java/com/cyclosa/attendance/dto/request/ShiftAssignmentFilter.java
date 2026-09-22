package com.cyclosa.attendance.dto.request;

import com.cyclosa.attendance.enums.ShiftAssignmentStatus;
import com.cyclosa.common.dto.request.BaseFilterRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Schema(description = "Tham số lọc lịch phân ca làm việc")
public class ShiftAssignmentFilter extends BaseFilterRequest {

    @Schema(description = "ID công ty")
    private UUID companyId;

    @Schema(description = "ID nhân viên")
    private UUID employeeId;

    @Schema(description = "ID ca làm việc")
    private UUID shiftId;

    @Schema(description = "Từ ngày", example = "2026-10-01")
    private LocalDate fromDate;

    @Schema(description = "Đến ngày", example = "2026-10-31")
    private LocalDate toDate;

    @Schema(description = "Trạng thái phân ca")
    private ShiftAssignmentStatus status;

    // --- Trường phục vụ DataScope nội bộ ---
    private UUID exactEmployeeId;
    private Set<UUID> allowedEmployeeIds;
}
