package com.cyclosa.organization.dto.response;

import com.cyclosa.common.enums.ActiveStatus;
import com.cyclosa.organization.enums.UnitType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Nút cây phân cấp đơn vị tổ chức")
public class OrgUnitTreeResponse {
    private UUID id;
    private String code;
    private String name;
    private UnitType unitType;
    private UUID parentUnitId;
    private UUID costCenterId;
    private String costCenterName;
    private UUID managerEmployeeId;
    private ActiveStatus status;

    @Builder.Default
    private List<OrgUnitTreeResponse> children = new ArrayList<>();
}
