package com.cyclosa.organization.mapper;

import com.cyclosa.organization.dto.request.CreateBranchRequest;
import com.cyclosa.organization.dto.request.UpdateBranchRequest;
import com.cyclosa.organization.dto.response.BranchResponse;
import com.cyclosa.organization.entity.Branch;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        builder = @Builder(disableBuilder = true),
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface BranchMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    Branch toEntity(CreateBranchRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "regionId", ignore = true)
    @Mapping(target = "code", ignore = true)
    void updateEntityFromDto(UpdateBranchRequest request, @MappingTarget Branch branch);

    @Mapping(target = "regionName", ignore = true)
    BranchResponse toResponse(Branch branch);

    List<BranchResponse> toResponseList(List<Branch> branches);
}

