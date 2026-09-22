package com.cyclosa.payroll.dto.request;

import com.cyclosa.common.dto.request.BaseFilterRequest;
import com.cyclosa.payroll.enums.SalaryAdvanceStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Schema(description = "Tham số lọc đơn tạm ứng lương")
public class SalaryAdvanceFilter extends BaseFilterRequest {

    @Schema(description = "ID công ty")
    private UUID companyId;

    @Schema(description = "ID nhân viên")
    private UUID employeeId;

    @Schema(description = "Trạng thái đơn tạm ứng")
    private SalaryAdvanceStatus status;

    @Schema(description = "Từ ngày")
    private LocalDate fromDate;

    @Schema(description = "Đến ngày")
    private LocalDate toDate;

    // --- DataScope nội bộ ---
    private UUID exactEmployeeId;
    private Set<UUID> allowedEmployeeIds;
}
