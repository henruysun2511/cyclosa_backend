package com.cyclosa.organization.mapper;

import com.cyclosa.common.dto.summary.CostCenterSummary;
import com.cyclosa.common.dto.summary.OrgUnitSummary;
import com.cyclosa.organization.dto.request.CreateOrgUnitRequest;
import com.cyclosa.organization.dto.response.OrgUnitDetailResponse;
import com.cyclosa.organization.dto.response.OrgUnitHistoryResponse;
import com.cyclosa.organization.dto.response.OrgUnitResponse;
import com.cyclosa.organization.dto.response.OrgUnitTreeResponse;
import com.cyclosa.organization.entity.CostCenter;
import com.cyclosa.organization.entity.OrganizationalUnit;
import com.cyclosa.organization.entity.OrganizationalUnitHistory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OrganizationalUnitMapper {

    @Mapping(target = "companyId", ignore = true)
    @Mapping(target = "parentUnit", ignore = true)
    @Mapping(target = "children", ignore = true)
    @Mapping(target = "costCenter", ignore = true)
    @Mapping(target = "status", ignore = true)
    OrganizationalUnit toEntity(CreateOrgUnitRequest request);

    @Mapping(target = "company", ignore = true)
    @Mapping(target = "parentUnit", source = "parentUnit")
    @Mapping(target = "costCenter", source = "costCenter")
    @Mapping(target = "manager", ignore = true)
    OrgUnitResponse toResponse(OrganizationalUnit unit);

    List<OrgUnitResponse> toResponseList(List<OrganizationalUnit> units);

    @Mapping(target = "company", ignore = true)
    @Mapping(target = "parentUnit", source = "parentUnit")
    @Mapping(target = "costCenter", source = "costCenter")
    @Mapping(target = "manager", ignore = true)
    @Mapping(target = "childUnitsCount", ignore = true)
    OrgUnitDetailResponse toDetailResponse(OrganizationalUnit unit);

    OrgUnitSummary toSummary(OrganizationalUnit unit);

    CostCenterSummary toCostCenterSummary(CostCenter costCenter);

    @Mapping(target = "parentUnitId", source = "parentUnit.id")
    @Mapping(target = "costCenterId", source = "costCenter.id")
    @Mapping(target = "costCenterName", source = "costCenter.name")
    @Mapping(target = "children", ignore = true)
    OrgUnitTreeResponse toTreeResponse(OrganizationalUnit unit);

    @Mapping(target = "parentUnitName", ignore = true)
    OrgUnitHistoryResponse toHistoryResponse(OrganizationalUnitHistory history);

    List<OrgUnitHistoryResponse> toHistoryResponseList(List<OrganizationalUnitHistory> histories);

    @Mapping(target = "id", source = "unitId")
    @Mapping(target = "costCenterName", ignore = true)
    @Mapping(target = "children", ignore = true)
    OrgUnitTreeResponse historyToTreeResponse(OrganizationalUnitHistory history);
}
