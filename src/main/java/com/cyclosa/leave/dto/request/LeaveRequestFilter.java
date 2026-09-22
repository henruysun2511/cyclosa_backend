package com.cyclosa.leave.dto.request;

import com.cyclosa.common.dto.request.BaseFilterRequest;
import com.cyclosa.leave.enums.LeaveCategory;
import com.cyclosa.leave.enums.LeaveRequestStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Schema(description = "Bộ lọc tìm kiếm đơn xin nghỉ phép")
public class LeaveRequestFilter extends BaseFilterRequest {

    @Schema(description = "ID công ty")
    private UUID companyId;

    @Schema(description = "ID nhân viên")
    private UUID employeeId;

    @Schema(description = "ID loại ngày nghỉ")
    private UUID leaveTypeId;

    @Schema(description = "Nhóm ngày nghỉ")
    private LeaveCategory category;

    @Schema(description = "Trạng thái đơn")
    private LeaveRequestStatus status;

    @Schema(description = "Lọc từ ngày")
    private LocalDate fromDate;

    @Schema(description = "Lọc đến ngày")
    private LocalDate toDate;

    // Dành cho bảo mật DataScope
    private UUID exactEmployeeId;
    private Set<UUID> allowedEmployeeIds;
}
