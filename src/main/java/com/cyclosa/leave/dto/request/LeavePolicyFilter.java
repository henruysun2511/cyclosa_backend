package com.cyclosa.leave.dto.request;

import com.cyclosa.common.dto.request.BaseFilterRequest;
import com.cyclosa.employee.enums.EmploymentType;
import com.cyclosa.leave.enums.JobConditionLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Schema(description = "Bộ lọc tìm kiếm chính sách nghỉ phép")
public class LeavePolicyFilter extends BaseFilterRequest {

    @Schema(description = "ID công ty")
    private UUID companyId;

    @Schema(description = "ID loại ngày nghỉ")
    private UUID leaveTypeId;

    @Schema(description = "Loại hình lao động")
    private EmploymentType applicableEmploymentType;

    @Schema(description = "Điều kiện làm việc")
    private JobConditionLevel jobConditionLevel;

    @Schema(description = "Trạng thái kích hoạt")
    private Boolean isActive;
}
