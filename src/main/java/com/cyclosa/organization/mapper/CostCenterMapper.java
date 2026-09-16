package com.cyclosa.organization.mapper;

import com.cyclosa.organization.dto.request.CreateCostCenterRequest;
import com.cyclosa.organization.dto.response.CostCenterResponse;
import com.cyclosa.organization.entity.CostCenter;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CostCenterMapper {

    @Mapping(target = "companyId", ignore = true)
    @Mapping(target = "status", ignore = true)
    CostCenter toEntity(CreateCostCenterRequest request);

    CostCenterResponse toResponse(CostCenter costCenter);

    List<CostCenterResponse> toResponseList(List<CostCenter> costCenters);
}
