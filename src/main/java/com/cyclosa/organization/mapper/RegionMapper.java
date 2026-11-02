package com.cyclosa.organization.mapper;

import com.cyclosa.organization.dto.request.CreateRegionRequest;
import com.cyclosa.organization.dto.request.UpdateRegionRequest;
import com.cyclosa.organization.dto.response.RegionResponse;
import com.cyclosa.organization.entity.Region;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        builder = @Builder(disableBuilder = true),
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface RegionMapper {

    @Mapping(target = "id", ignore = true)
    Region toEntity(CreateRegionRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "companyId", ignore = true)
    @Mapping(target = "code", ignore = true)
    void updateEntityFromDto(UpdateRegionRequest request, @MappingTarget Region region);

    @Mapping(target = "branchCount", ignore = true)
    RegionResponse toResponse(Region region);

    List<RegionResponse> toResponseList(List<Region> regions);
}

