package com.cyclosa.organization.service;

import com.cyclosa.organization.dto.request.CreateBranchRequest;
import com.cyclosa.organization.dto.request.CreateRegionRequest;
import com.cyclosa.organization.dto.request.UpdateBranchRequest;
import com.cyclosa.organization.dto.response.BranchResponse;
import com.cyclosa.organization.dto.response.RegionResponse;

import java.util.List;
import java.util.UUID;

public interface GeographyService {

    RegionResponse createRegion(UUID companyId, CreateRegionRequest request);

    List<RegionResponse> getGeographyTree(UUID companyId);

    BranchResponse createBranch(UUID companyId, CreateBranchRequest request);

    BranchResponse updateBranch(UUID companyId, UUID id, UpdateBranchRequest request);

    BranchResponse getBranchById(UUID companyId, UUID id);

    List<BranchResponse> getBranches(UUID companyId);
}
