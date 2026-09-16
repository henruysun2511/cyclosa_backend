package com.cyclosa.organization.dto.response;

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
@Schema(description = "Dự báo tác động khi tái cơ cấu đơn vị tổ chức")
public class OrgUnitImpactPreviewResponse {
    private UUID unitId;
    private String unitName;
    private UUID currentParentId;
    private String currentParentName;
    private UUID targetParentId;
    private String targetParentName;
    private int affectedSubUnitsCount;
    private int affectedEmployeesCount;
    private boolean isCircularLoop;
    private String message;
}
