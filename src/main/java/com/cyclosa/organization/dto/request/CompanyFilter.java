package com.cyclosa.organization.dto.request;

import com.cyclosa.common.dto.request.BaseFilterRequest;
import com.cyclosa.common.enums.ActiveStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Tham số tìm kiếm và phân trang danh sách công ty")
public class CompanyFilter extends BaseFilterRequest {

    @Schema(description = "Trạng thái hoạt động", example = "ACTIVE")
    private ActiveStatus status;
}
