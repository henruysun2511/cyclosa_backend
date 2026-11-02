package com.cyclosa.organization.dto.request;

import com.cyclosa.common.dto.request.BaseFilterRequest;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Tham số tìm kiếm và phân trang công ty")
public class CompanyFilterRequest extends BaseFilterRequest {

    @Parameter(description = "Trạng thái hoạt động (ACTIVE, INACTIVE)")
    private String status;
}
