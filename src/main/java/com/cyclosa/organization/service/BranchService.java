package com.cyclosa.organization.service;

import com.cyclosa.common.exception.AppException;
import com.cyclosa.common.response.PageData;
import com.cyclosa.common.util.PageableUtils;
import com.cyclosa.organization.dto.request.BranchFilterRequest;
import com.cyclosa.organization.dto.request.CreateBranchRequest;
import com.cyclosa.organization.dto.request.UpdateBranchRequest;
import com.cyclosa.organization.dto.response.BranchResponse;
import com.cyclosa.organization.entity.Branch;
import com.cyclosa.organization.entity.Region;
import com.cyclosa.organization.exception.OrganizationErrorCode;
import com.cyclosa.organization.mapper.BranchMapper;
import com.cyclosa.organization.repository.BranchRepository;
import com.cyclosa.organization.repository.RegionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BranchService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("code", "name", "isActive", "createdAt");

    private final BranchRepository branchRepository;
    private final RegionRepository regionRepository;
    private final BranchMapper branchMapper;

    public PageData<BranchResponse> getBranches(BranchFilterRequest filter) {
        Region region = findRegionOrThrow(filter.getRegionId());
        String kw = PageableUtils.normalizeKeyword(filter.getKeyword());
        Pageable pageable = filter.toPageable("createdAt", ALLOWED_SORT_FIELDS);
        Page<Branch> page = branchRepository.search(filter.getRegionId(), kw, filter.getIsActive(), pageable);
        return PageData.of(page, b -> mapToResponse(b, region.getName()));
    }

    public List<BranchResponse> getBranchesByRegion(UUID regionId) {
        Region region = findRegionOrThrow(regionId);
        return branchRepository.findAllByRegionId(regionId).stream()
                .map(b -> mapToResponse(b, region.getName()))
                .toList();
    }

    public List<BranchResponse> getBranchesByCompany(UUID companyId) {
        List<Region> regions = regionRepository.findAllByCompanyId(companyId);
        Map<UUID, String> regionNameMap = regions.stream()
                .collect(Collectors.toMap(Region::getId, Region::getName));

        return branchRepository.findAllByCompanyId(companyId).stream()
                .map(b -> mapToResponse(b, regionNameMap.getOrDefault(b.getRegionId(), "")))
                .toList();
    }

    public BranchResponse getBranchById(UUID id) {
        Branch branch = findBranchOrThrow(id);
        Region region = findRegionOrThrow(branch.getRegionId());
        return mapToResponse(branch, region.getName());
    }

    @Transactional
    public BranchResponse createBranch(CreateBranchRequest request) {
        Region region = findRegionOrThrow(request.getRegionId());

        if (branchRepository.existsByRegionIdAndCode(request.getRegionId(), request.getCode())) {
            throw new AppException(OrganizationErrorCode.BRANCH_CODE_EXISTS);
        }

        Branch branch = branchMapper.toEntity(request);
        branch.setCode(request.getCode().toUpperCase().trim());
        branch.setName(request.getName().trim());

        branch = branchRepository.save(branch);
        log.info("Đã tạo mới chi nhánh: id={}, code={}", branch.getId(), branch.getCode());
        return mapToResponse(branch, region.getName());
    }

    @Transactional
    public BranchResponse updateBranch(UUID id, UpdateBranchRequest request) {
        Branch branch = findBranchOrThrow(id);
        Region region = findRegionOrThrow(branch.getRegionId());

        branchMapper.updateEntityFromDto(request, branch);
        branch.setName(request.getName().trim());

        branch = branchRepository.save(branch);
        log.info("Đã cập nhật chi nhánh: id={}", branch.getId());
        return mapToResponse(branch, region.getName());
    }

    @Transactional
    public void deleteBranch(UUID id) {
        Branch branch = findBranchOrThrow(id);
        branchRepository.delete(branch);
        log.info("Đã xóa mềm chi nhánh: id={}", id);
    }

    public Branch findBranchOrThrow(UUID id) {
        return branchRepository.findById(id)
                .orElseThrow(() -> new AppException(OrganizationErrorCode.BRANCH_NOT_FOUND));
    }

    private Region findRegionOrThrow(UUID regionId) {
        return regionRepository.findById(regionId)
                .orElseThrow(() -> new AppException(OrganizationErrorCode.REGION_NOT_FOUND));
    }

    private BranchResponse mapToResponse(Branch branch, String regionName) {
        BranchResponse res = branchMapper.toResponse(branch);
        res.setRegionName(regionName);
        return res;
    }
}
