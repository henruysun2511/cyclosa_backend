package com.cyclosa.payroll.dto.request;

import com.cyclosa.common.dto.request.BaseFilterRequest;
import com.cyclosa.payroll.enums.PayrollPeriodStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Schema(description = "Tham số lọc danh sách kỳ lương")
public class PayrollPeriodFilter extends BaseFilterRequest {

    @Schema(description = "ID công ty")
    private UUID companyId;

    @Schema(description = "Tháng tính lương")
    private Integer month;

    @Schema(description = "Năm tính lương")
    private Integer year;

    @Schema(description = "Trạng thái kỳ lương")
    private PayrollPeriodStatus status;
}
