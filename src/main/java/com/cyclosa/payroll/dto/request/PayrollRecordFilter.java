package com.cyclosa.payroll.dto.request;

import com.cyclosa.common.dto.request.BaseFilterRequest;
import com.cyclosa.payroll.enums.PayrollRecordStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Schema(description = "Tham số lọc bảng lương nhân sự")
public class PayrollRecordFilter extends BaseFilterRequest {

    @Schema(description = "ID công ty")
    private UUID companyId;

    @Schema(description = "ID kỳ lương")
    private UUID payrollPeriodId;

    @Schema(description = "ID nhân viên")
    private UUID employeeId;

    @Schema(description = "Trạng thái bản ghi lương")
    private PayrollRecordStatus status;

    // --- DataScope nội bộ ---
    private UUID exactEmployeeId;
    private Set<UUID> allowedEmployeeIds;
}
