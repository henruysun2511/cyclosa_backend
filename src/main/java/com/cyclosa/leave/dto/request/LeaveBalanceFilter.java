package com.cyclosa.leave.dto.request;

import com.cyclosa.common.dto.request.BaseFilterRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Schema(description = "Bộ lọc tìm kiếm quỹ phép của nhân viên")
public class LeaveBalanceFilter extends BaseFilterRequest {

    @Schema(description = "ID công ty")
    private UUID companyId;

    @Schema(description = "ID nhân viên")
    private UUID employeeId;

    @Schema(description = "ID loại ngày nghỉ")
    private UUID leaveTypeId;

    @Schema(description = "Năm tính quỹ phép")
    private Integer year;

    // Dành cho bảo mật DataScope
    private UUID exactEmployeeId;
    private Set<UUID> allowedEmployeeIds;
}
