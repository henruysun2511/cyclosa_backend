package com.cyclosa.organization.dto.response;

import com.cyclosa.organization.enums.UnitType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Bản ghi lịch sử phiên bản cơ cấu của đơn vị theo thời gian")
public class OrgUnitHistoryResponse {
    private UUID id;
    private UUID unitId;
    private UUID companyId;
    private UUID parentUnitId;
    private String parentUnitName;
    private String code;
    private String name;
    private UnitType unitType;
    private UUID managerEmployeeId;
    private LocalDateTime effectiveFrom;
    private LocalDateTime effectiveTo;
    private String changeReason;
}
