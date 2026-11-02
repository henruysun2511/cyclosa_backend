package com.cyclosa.organization.service;

import com.cyclosa.common.exception.AppException;
import com.cyclosa.common.response.PageData;
import com.cyclosa.organization.dto.request.CreateBranchRequest;
import com.cyclosa.organization.dto.request.UpdateBranchRequest;
import com.cyclosa.organization.dto.response.BranchResponse;
import com.cyclosa.organization.entity.Branch;
import com.cyclosa.organization.entity.Region;
import com.cyclosa.organization.exception.OrganizationErrorCode;
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
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BranchService {

    private final BranchRepository branchRepository;
    private final RegionRepository regionRepository;

    public PageData<BranchResponse> getBranches(UUID regionId, String keyword, Boolean isActive, Pageable pageable) {
        Region region = findRegionOrThrow(regionId);
        Page<Branch> page = branchRepository.search(regionId, keyword, isActive, pageable);
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

        Branch branch = Branch.builder()
                .regionId(request.getRegionId())
                .code(request.getCode().toUpperCase().trim())
                .name(request.getName().trim())
                .address(request.getAddress())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .attendanceRadiusMeters(request.getAttendanceRadiusMeters() != null ? request.getAttendanceRadiusMeters() : 100)
                .isActive(true)
                .deleted(false)
                .build();

        branch = branchRepository.save(branch);
        log.info("Đã tạo mới chi nhánh: id={}, code={}", branch.getId(), branch.getCode());
        return mapToResponse(branch, region.getName());
    }

    @Transactional
    public BranchResponse updateBranch(UUID id, UpdateBranchRequest request) {
        Branch branch = findBranchOrThrow(id);
        Region region = findRegionOrThrow(branch.getRegionId());

        branch.setName(request.getName().trim());
        branch.setAddress(request.getAddress());
        branch.setLatitude(request.getLatitude());
        branch.setLongitude(request.getLongitude());
        if (request.getAttendanceRadiusMeters() != null) {
            branch.setAttendanceRadiusMeters(request.getAttendanceRadiusMeters());
        }
        if (request.getIsActive() != null) {
            branch.setIsActive(request.getIsActive());
        }

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
        return BranchResponse.builder()
                .id(branch.getId())
                .regionId(branch.getRegionId())
                .regionName(regionName)
                .code(branch.getCode())
                .name(branch.getName())
                .address(branch.getAddress())
                .latitude(branch.getLatitude())
                .longitude(branch.getLongitude())
                .attendanceRadiusMeters(branch.getAttendanceRadiusMeters())
                .isActive(branch.getIsActive())
                .createdAt(branch.getCreatedAt())
                .updatedAt(branch.getUpdatedAt())
                .build();
    }
}

