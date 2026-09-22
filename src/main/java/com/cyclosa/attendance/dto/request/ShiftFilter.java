package com.cyclosa.attendance.dto.request;

import com.cyclosa.common.dto.request.BaseFilterRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Schema(description = "Tham số lọc ca làm việc")
public class ShiftFilter extends BaseFilterRequest {

    @Schema(description = "ID công ty")
    private UUID companyId;

    @Schema(description = "Lọc theo trạng thái hoạt động")
    private Boolean isActive;
}
