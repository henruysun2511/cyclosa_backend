package com.cyclosa.organization.dto.response;

import com.cyclosa.common.enums.ActiveStatus;
import com.cyclosa.organization.enums.UnitType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
@Schema(description = "Thông tin chi tiết đơn vị tổ chức")
public class OrgUnitResponse {
    private UUID id;
    private UUID companyId;
    private String code;
    private String name;
    private UnitType unitType;
    private UUID parentUnitId;
    private String parentUnitName;
    private UUID costCenterId;
    private String costCenterName;
    private UUID managerEmployeeId;
    private String description;
    private ActiveStatus status;
}
