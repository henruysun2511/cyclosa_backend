package com.cyclosa.attendance.dto.request;

import com.cyclosa.attendance.enums.ExplanationReasonType;
import com.cyclosa.attendance.enums.ExplanationStatus;
import com.cyclosa.common.dto.request.BaseFilterRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Schema(description = "Tham số lọc đơn giải trình chấm công")
public class ExplanationFilter extends BaseFilterRequest {

    @Schema(description = "ID công ty")
    private UUID companyId;

    @Schema(description = "ID nhân viên làm đơn")
    private UUID employeeId;

    @Schema(description = "Trạng thái đơn")
    private ExplanationStatus status;

    @Schema(description = "Loại lý do giải trình")
    private ExplanationReasonType reasonType;

    @Schema(description = "Từ ngày", example = "2026-10-01")
    private LocalDate fromDate;

    @Schema(description = "Đến ngày", example = "2026-10-31")
    private LocalDate toDate;

    // --- Trường phục vụ DataScope nội bộ ---
    private UUID exactEmployeeId;
    private Set<UUID> allowedEmployeeIds;
}
