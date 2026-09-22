package com.cyclosa.leave.dto.request;

import com.cyclosa.employee.enums.EmploymentType;
import com.cyclosa.leave.enums.JobConditionLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu tạo chính sách ngày nghỉ phép")
public class CreateLeavePolicyRequest {

    @NotNull(message = "Loại nghỉ phép không được để trống")
    @Schema(description = "ID loại ngày nghỉ")
    private UUID leaveTypeId;

    @Schema(description = "Loại hình lao động áp dụng (FULL_TIME, PART_TIME...)")
    private EmploymentType applicableEmploymentType;

    @NotNull(message = "Điều kiện làm việc không được để trống")
    @Schema(description = "Điều kiện làm việc (NORMAL, HAZARDOUS, SPECIAL_HAZARDOUS)", example = "NORMAL")
    @Builder.Default
    private JobConditionLevel jobConditionLevel = JobConditionLevel.NORMAL;

    @NotNull(message = "Định mức ngày phép năm không được để trống")
    @DecimalMin(value = "0.0", message = "Số ngày phép cơ bản không được âm")
    @Schema(description = "Số ngày phép cơ bản/năm theo Điều 113", example = "12.0")
    @Builder.Default
    private BigDecimal accrualDaysPerYear = BigDecimal.valueOf(12.0);

    @Schema(description = "Cứ bao nhiêu năm thâm niên thì cộng thêm ngày (Điều 114)", example = "5")
    @Builder.Default
    private Integer seniorityBonusEveryYears = 5;

    @Schema(description = "Số ngày cộng thêm mỗi bậc thâm niên", example = "1.0")
    @Builder.Default
    private BigDecimal seniorityBonusDays = BigDecimal.valueOf(1.0);

    @Schema(description = "Số ngày tối đa được phép chuyển sang năm sau", example = "5.0")
    private BigDecimal carryOverMaxDays;

    @Schema(description = "Tháng hết hạn phép chuyển (mặc định 3 tức 31/03)", example = "3")
    @Builder.Default
    private Integer carryOverExpiryMonth = 3;

    @Schema(description = "Ngày bắt đầu áp dụng chính sách")
    private LocalDate effectiveDate;
}
