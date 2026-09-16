package com.cyclosa.organization.dto.response;

import com.cyclosa.common.enums.ActiveStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin vị trí chức danh")
public class PositionResponse {
    private UUID id;
    private UUID companyId;
    private String code;
    private String name;
    private UUID jobLevelId;
    private String jobLevelName;
    private Integer rankOrder;
    private String description;
    private ActiveStatus status;
}
