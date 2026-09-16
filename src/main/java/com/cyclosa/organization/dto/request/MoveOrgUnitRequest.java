package com.cyclosa.organization.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Schema(description = "Yêu cầu điều chuyển đơn vị sang nhánh cha mới")
public class MoveOrgUnitRequest {

    @Schema(description = "ID đơn vị cha mới (để null nếu muốn chuyển thành cấp cao nhất - Root unit)", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID targetParentId;
}
