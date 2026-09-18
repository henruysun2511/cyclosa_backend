package com.cyclosa.organization.mapper;

import com.cyclosa.common.dto.summary.RegionSummary;
import com.cyclosa.organization.dto.request.CreateBranchRequest;
import com.cyclosa.organization.dto.request.CreateRegionRequest;
import com.cyclosa.organization.dto.response.BranchDetailResponse;
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

    @Mapping(target = "company", ignore = true)
    RegionResponse toResponse(Region region);

    List<RegionResponse> toRegionResponseList(List<Region> regions);

    @Mapping(target = "companyId", ignore = true)
    @Mapping(target = "region", ignore = true)
    @Mapping(target = "status", ignore = true)
    Branch toEntity(CreateBranchRequest request);

    @Mapping(target = "company", ignore = true)
    @Mapping(target = "region", source = "region")
    BranchResponse toResponse(Branch branch);

    List<BranchResponse> toBranchResponseList(List<Branch> branches);

    @Mapping(target = "company", ignore = true)
    @Mapping(target = "region", source = "region")
    BranchDetailResponse toDetailResponse(Branch branch);

    RegionSummary toRegionSummary(Region region);
}
