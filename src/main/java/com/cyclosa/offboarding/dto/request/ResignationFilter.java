package com.cyclosa.offboarding.dto.request;

import com.cyclosa.common.dto.request.BaseFilterRequest;
import com.cyclosa.offboarding.enums.ResignationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Bộ lọc tìm kiếm đơn xin thôi việc")
public class ResignationFilter extends BaseFilterRequest {

    @Schema(description = "Lọc theo công ty")
    private UUID companyId;

    @Schema(description = "Lọc theo nhân viên nộp đơn")
    private UUID employeeId;

    @Schema(description = "Lọc theo trạng thái đơn thôi việc")
    private ResignationStatus status;
}
