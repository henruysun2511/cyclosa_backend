package com.cyclosa.organization.service;

import com.cyclosa.common.exception.AppException;
import com.cyclosa.common.response.PageData;
import com.cyclosa.organization.dto.request.CreateRegionRequest;
import com.cyclosa.organization.dto.request.UpdateRegionRequest;
import com.cyclosa.organization.dto.response.RegionResponse;
import com.cyclosa.organization.entity.Region;
import com.cyclosa.organization.exception.OrganizationErrorCode;
import com.cyclosa.organization.repository.BranchRepository;
import com.cyclosa.organization.repository.CompanyRepository;
import com.cyclosa.organization.repository.RegionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RegionService {

    private final RegionRepository regionRepository;
    private final CompanyRepository companyRepository;
    private final BranchRepository branchRepository;

    public PageData<RegionResponse> getRegions(UUID companyId, String keyword, Pageable pageable) {
        verifyCompanyExists(companyId);
        Page<Region> page = regionRepository.search(companyId, keyword, pageable);
        return PageData.of(page, this::mapToResponse);
    }

    public List<RegionResponse> getAllRegionsByCompany(UUID companyId) {
        verifyCompanyExists(companyId);
        return regionRepository.findAllByCompanyId(companyId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public RegionResponse getRegionById(UUID id) {
        Region region = findRegionOrThrow(id);
        return mapToResponse(region);
    }

    @Transactional
    public RegionResponse createRegion(CreateRegionRequest request) {
        verifyCompanyExists(request.getCompanyId());

        if (regionRepository.existsByCompanyIdAndCode(request.getCompanyId(), request.getCode())) {
            throw new AppException(OrganizationErrorCode.REGION_CODE_EXISTS);
        }

        Region region = Region.builder()
                .companyId(request.getCompanyId())
                .code(request.getCode().toUpperCase().trim())
                .name(request.getName().trim())
                .deleted(false)
                .build();

        region = regionRepository.save(region);
        log.info("Đã tạo mới khu vực: id={}, code={}", region.getId(), region.getCode());
        return mapToResponse(region);
    }

    @Transactional
    public RegionResponse updateRegion(UUID id, UpdateRegionRequest request) {
        Region region = findRegionOrThrow(id);
        region.setName(request.getName().trim());
        region = regionRepository.save(region);
        log.info("Đã cập nhật khu vực: id={}", region.getId());
        return mapToResponse(region);
    }

    @Transactional
    public void deleteRegion(UUID id) {
        Region region = findRegionOrThrow(id);
        regionRepository.delete(region);
        log.info("Đã xóa mềm khu vực: id={}", id);
    }

    public Region findRegionOrThrow(UUID id) {
        return regionRepository.findById(id)
                .orElseThrow(() -> new AppException(OrganizationErrorCode.REGION_NOT_FOUND));
    }

    private void verifyCompanyExists(UUID companyId) {
        if (!companyRepository.existsById(companyId)) {
            throw new AppException(OrganizationErrorCode.COMPANY_NOT_FOUND);
        }
    }

    private RegionResponse mapToResponse(Region region) {
        int branchCount = branchRepository.findAllByRegionId(region.getId()).size();
        return RegionResponse.builder()
                .id(region.getId())
                .companyId(region.getCompanyId())
                .code(region.getCode())
                .name(region.getName())
                .branchCount(branchCount)
                .createdAt(region.getCreatedAt())
                .updatedAt(region.getUpdatedAt())
                .build();
    }
}

