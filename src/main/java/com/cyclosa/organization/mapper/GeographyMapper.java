package com.cyclosa.organization.mapper;

import com.cyclosa.organization.dto.request.CreateBranchRequest;
import com.cyclosa.organization.dto.request.CreateRegionRequest;
import com.cyclosa.organization.dto.response.BranchResponse;
import com.cyclosa.organization.dto.response.RegionResponse;
import com.cyclosa.organization.entity.Branch;
import com.cyclosa.organization.entity.Region;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface GeographyMapper {

    @Mapping(target = "companyId", ignore = true)
    @Mapping(target = "branches", ignore = true)
    @Mapping(target = "status", ignore = true)
    Region toEntity(CreateRegionRequest request);

    RegionResponse toResponse(Region region);

    List<RegionResponse> toRegionResponseList(List<Region> regions);

    @Mapping(target = "companyId", ignore = true)
    @Mapping(target = "region", ignore = true)
    @Mapping(target = "status", ignore = true)
    Branch toEntity(CreateBranchRequest request);

    @Mapping(target = "regionId", source = "region.id")
    @Mapping(target = "regionName", source = "region.name")
    BranchResponse toResponse(Branch branch);

    List<BranchResponse> toBranchResponseList(List<Branch> branches);
}
