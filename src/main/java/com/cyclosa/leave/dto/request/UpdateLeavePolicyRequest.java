package com.cyclosa.leave.dto.request;

import com.cyclosa.employee.enums.EmploymentType;
import com.cyclosa.leave.enums.JobConditionLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu cập nhật chính sách ngày nghỉ phép")
public class UpdateLeavePolicyRequest {

    @Schema(description = "Loại hình lao động áp dụng")
    private EmploymentType applicableEmploymentType;

    @Schema(description = "Điều kiện làm việc")
    private JobConditionLevel jobConditionLevel;

    @DecimalMin(value = "0.0", message = "Số ngày phép cơ bản không được âm")
    @Schema(description = "Số ngày phép cơ bản/năm theo Điều 113")
    private BigDecimal accrualDaysPerYear;

    @Schema(description = "Cứ bao nhiêu năm thâm niên thì cộng thêm ngày (Điều 114)")
    private Integer seniorityBonusEveryYears;

    @Schema(description = "Số ngày cộng thêm mỗi bậc thâm niên")
    private BigDecimal seniorityBonusDays;

    @Schema(description = "Số ngày tối đa được phép chuyển sang năm sau")
    private BigDecimal carryOverMaxDays;

    @Schema(description = "Tháng hết hạn phép chuyển")
    private Integer carryOverExpiryMonth;

    @Schema(description = "Ngày bắt đầu áp dụng chính sách")
    private LocalDate effectiveDate;

    @Schema(description = "Trạng thái kích hoạt")
    private Boolean isActive;
}
