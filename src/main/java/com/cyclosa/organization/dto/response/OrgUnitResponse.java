package com.cyclosa.organization.dto.response;

import com.cyclosa.common.dto.summary.CompanySummary;
import com.cyclosa.common.dto.summary.CostCenterSummary;
import com.cyclosa.common.dto.summary.EmployeeSummary;
import com.cyclosa.common.dto.summary.OrgUnitSummary;
import com.cyclosa.common.enums.ActiveStatus;
import com.cyclosa.organization.enums.UnitType;
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
@Schema(description = "Thông tin tóm tắt đơn vị tổ chức trong danh sách")
public class OrgUnitResponse {
    private UUID id;
    private CompanySummary company;
    private String code;
    private String name;
    private UnitType unitType;
    private OrgUnitSummary parentUnit;
    private CostCenterSummary costCenter;
    private EmployeeSummary manager;
    private ActiveStatus status;
}
