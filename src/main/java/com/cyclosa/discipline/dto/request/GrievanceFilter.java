package com.cyclosa.discipline.dto.request;

import com.cyclosa.common.dto.request.BaseFilterRequest;
import com.cyclosa.discipline.enums.GrievanceStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Bộ lọc tìm kiếm đơn khiếu nại")
public class GrievanceFilter extends BaseFilterRequest {

    @Schema(description = "Lọc theo công ty")
    private UUID companyId;

    @Schema(description = "Lọc theo nhân viên gửi khiếu nại")
    private UUID employeeId;

    @Schema(description = "Lọc theo trạng thái xử lý khiếu nại")
    private GrievanceStatus status;
}
