package com.cyclosa.attendance.dto.request;

import com.cyclosa.common.dto.request.BaseFilterRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Schema(description = "Tham số lọc bảng công tổng hợp tháng")
public class TimesheetFilter extends BaseFilterRequest {

    @Schema(description = "ID công ty")
    private UUID companyId;

    @Schema(description = "ID nhân viên")
    private UUID employeeId;

    @Schema(description = "Tháng", example = "10")
    private Integer month;

    @Schema(description = "Năm", example = "2026")
    private Integer year;

    @Schema(description = "Lọc theo trạng thái khóa chốt")
    private Boolean isLocked;

    // --- Trường phục vụ DataScope nội bộ ---
    private UUID exactEmployeeId;
    private Set<UUID> allowedEmployeeIds;
}
