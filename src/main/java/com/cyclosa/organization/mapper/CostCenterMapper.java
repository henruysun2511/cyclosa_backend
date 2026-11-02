package com.cyclosa.organization.mapper;

import com.cyclosa.organization.dto.request.CreateCostCenterRequest;
import com.cyclosa.organization.dto.request.UpdateCostCenterRequest;
import com.cyclosa.organization.dto.response.CostCenterResponse;
import com.cyclosa.organization.entity.CostCenter;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        builder = @Builder(disableBuilder = true),
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface CostCenterMapper {

    @Mapping(target = "id", ignore = true)
    CostCenter toEntity(CreateCostCenterRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "companyId", ignore = true)
    @Mapping(target = "code", ignore = true)
    void updateEntityFromDto(UpdateCostCenterRequest request, @MappingTarget CostCenter costCenter);

    CostCenterResponse toResponse(CostCenter costCenter);

    List<CostCenterResponse> toResponseList(List<CostCenter> costCenters);
}

