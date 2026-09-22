package com.cyclosa.leave.dto.request;

import com.cyclosa.leave.enums.FundingSource;
import com.cyclosa.leave.enums.LeaveCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu tạo loại ngày nghỉ phép")
public class CreateLeaveTypeRequest {

    @NotBlank(message = "Tên loại nghỉ phép không được để trống")
    @Size(max = 100, message = "Tên loại nghỉ phép tối đa 100 ký tự")
    @Schema(description = "Tên loại ngày nghỉ", example = "Nghỉ phép năm")
    private String name;

    @NotBlank(message = "Mã loại nghỉ phép không được để trống")
    @Size(max = 50, message = "Mã loại nghỉ phép tối đa 50 ký tự")
    @Schema(description = "Mã định danh", example = "ANNUAL_LEAVE")
    private String code;

    @NotNull(message = "Phân nhóm loại phép không được để trống")
    @Schema(description = "Nhóm bản chất pháp lý (ANNUAL, PUBLIC_HOLIDAY, PERSONAL_PAID, PERSONAL_UNPAID, SICK, MATERNITY)", example = "ANNUAL")
    private LeaveCategory category;

    @Schema(description = "Nguồn chi trả (COMPANY, SOCIAL_INSURANCE_FUND)", example = "COMPANY")
    @Builder.Default
    private FundingSource fundingSource = FundingSource.COMPANY;

    @Schema(description = "Số ngày cố định per sự kiện (dùng cho Điều 115 việc riêng)", example = "3.0")
    private BigDecimal fixedDaysPerEvent;

    @Schema(description = "Hưởng nguyên lương hay không", example = "true")
    @Builder.Default
    private Boolean isPaid = true;

    @Schema(description = "Có yêu cầu phê duyệt qua Workflow không", example = "true")
    @Builder.Default
    private Boolean requiresApproval = true;

    @Schema(description = "Mô tả chi tiết")
    private String description;
}
