package com.cyclosa.organization.dto.request;

import com.cyclosa.common.dto.request.BaseFilterRequest;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompanyFilterRequest extends BaseFilterRequest {

    @Parameter(description = "Trạng thái hoạt động (ACTIVE, INACTIVE)")
    private String status;
}

