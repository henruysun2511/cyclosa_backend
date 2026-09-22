package com.cyclosa.leave.dto.request;

import com.cyclosa.common.dto.request.BaseFilterRequest;
import com.cyclosa.leave.enums.FundingSource;
import com.cyclosa.leave.enums.LeaveCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Schema(description = "Bộ lọc tìm kiếm loại ngày nghỉ")
public class LeaveTypeFilter extends BaseFilterRequest {

    @Schema(description = "ID công ty")
    private UUID companyId;

    @Schema(description = "Nhóm ngày nghỉ")
    private LeaveCategory category;

    @Schema(description = "Nguồn kinh phí")
    private FundingSource fundingSource;

    @Schema(description = "Trạng thái có lương hay không")
    private Boolean isPaid;

    @Schema(description = "Trạng thái kích hoạt")
    private Boolean isActive;
}
