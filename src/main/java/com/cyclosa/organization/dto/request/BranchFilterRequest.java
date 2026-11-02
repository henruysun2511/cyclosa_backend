package com.cyclosa.organization.dto.request;

import com.cyclosa.common.dto.request.BaseFilterRequest;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Schema(description = "Tham số tìm kiếm và phân trang chi nhánh")
public class BranchFilterRequest extends BaseFilterRequest {

    @NotNull(message = "ID khu vực không được để trống")
    @Parameter(description = "ID khu vực trực thuộc", required = true)
    private UUID regionId;

    @Parameter(description = "Lọc theo trạng thái hoạt động")
    private Boolean isActive;
}

