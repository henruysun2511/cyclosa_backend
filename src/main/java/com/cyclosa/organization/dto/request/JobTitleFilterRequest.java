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
@Schema(description = "Tham số tìm kiếm và phân trang chức danh công việc")
public class JobTitleFilterRequest extends BaseFilterRequest {

    @NotNull(message = "ID công ty không được để trống")
    @Parameter(description = "ID công ty trực thuộc", required = true)
    private UUID companyId;
}

