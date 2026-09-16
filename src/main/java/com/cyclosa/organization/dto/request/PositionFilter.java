package com.cyclosa.organization.dto.request;

import com.cyclosa.common.dto.request.BaseFilterRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Schema(description = "Tham số tìm kiếm và phân trang vị trí chức danh")
public class PositionFilter extends BaseFilterRequest {

    @Schema(description = "Lọc theo ID cấp bậc", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID jobLevelId;
}
