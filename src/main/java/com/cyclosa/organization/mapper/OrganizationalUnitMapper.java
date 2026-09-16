package com.cyclosa.organization.mapper;

import com.cyclosa.organization.dto.request.CreateOrgUnitRequest;
import com.cyclosa.organization.dto.response.OrgUnitResponse;
import com.cyclosa.organization.dto.response.OrgUnitTreeResponse;
import com.cyclosa.organization.entity.OrganizationalUnit;
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

    @Mapping(target = "parentUnitId", source = "parentUnit.id")
    @Mapping(target = "parentUnitName", source = "parentUnit.name")
    @Mapping(target = "costCenterId", source = "costCenter.id")
    @Mapping(target = "costCenterName", source = "costCenter.name")
    OrgUnitResponse toResponse(OrganizationalUnit unit);

    List<OrgUnitResponse> toResponseList(List<OrganizationalUnit> units);

    @Mapping(target = "parentUnitId", source = "parentUnit.id")
    @Mapping(target = "costCenterId", source = "costCenter.id")
    @Mapping(target = "costCenterName", source = "costCenter.name")
    @Mapping(target = "children", ignore = true)
    OrgUnitTreeResponse toTreeResponse(OrganizationalUnit unit);
}
