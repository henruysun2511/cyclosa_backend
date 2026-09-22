package com.cyclosa.leave.dto.request;

import com.cyclosa.leave.enums.FundingSource;
import com.cyclosa.leave.enums.LeaveCategory;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Yêu cầu cập nhật loại ngày nghỉ phép")
public class UpdateLeaveTypeRequest {

    @Size(max = 100, message = "Tên loại nghỉ phép tối đa 100 ký tự")
    @Schema(description = "Tên loại ngày nghỉ", example = "Nghỉ phép năm")
    private String name;

    @Schema(description = "Nhóm bản chất pháp lý")
    private LeaveCategory category;

    @Schema(description = "Nguồn chi trả")
    private FundingSource fundingSource;

    @Schema(description = "Số ngày cố định per sự kiện")
    private BigDecimal fixedDaysPerEvent;

    @Schema(description = "Hưởng nguyên lương hay không")
    private Boolean isPaid;

    @Schema(description = "Có yêu cầu phê duyệt qua Workflow không")
    private Boolean requiresApproval;

    @Schema(description = "Trạng thái kích hoạt")
    private Boolean isActive;

    @Schema(description = "Mô tả chi tiết")
    private String description;
}
