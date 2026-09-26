package com.cyclosa.offboarding.dto.request;

import com.cyclosa.common.dto.request.BaseFilterRequest;
import com.cyclosa.offboarding.enums.TerminationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Bộ lọc tìm kiếm quyết định chấm dứt hợp đồng lao động")
public class TerminationFilter extends BaseFilterRequest {

    @Schema(description = "Lọc theo công ty")
    private UUID companyId;

    @Schema(description = "Lọc theo nhân viên bị chấm dứt")
    private UUID employeeId;

    @Schema(description = "Lọc theo trạng thái quyết định")
    private TerminationStatus status;
}
