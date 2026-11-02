package com.cyclosa.organization.service;

import com.cyclosa.common.exception.AppException;
import com.cyclosa.common.response.PageData;
import com.cyclosa.common.util.PageableUtils;
import com.cyclosa.organization.dto.request.CreateRegionRequest;
import com.cyclosa.organization.dto.request.RegionFilterRequest;
import com.cyclosa.organization.dto.request.UpdateRegionRequest;
import com.cyclosa.organization.dto.response.RegionResponse;
import com.cyclosa.organization.entity.Region;
import com.cyclosa.organization.exception.OrganizationErrorCode;
import com.cyclosa.organization.mapper.RegionMapper;
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
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RegionService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("code", "name", "createdAt");

    private final RegionRepository regionRepository;
    private final CompanyRepository companyRepository;
    private final BranchRepository branchRepository;
    private final RegionMapper regionMapper;

    public PageData<RegionResponse> getRegions(RegionFilterRequest filter) {
        verifyCompanyExists(filter.getCompanyId());
        String kw = PageableUtils.normalizeKeyword(filter.getKeyword());
        Pageable pageable = filter.toPageable("createdAt", ALLOWED_SORT_FIELDS);
        Page<Region> page = regionRepository.search(filter.getCompanyId(), kw, pageable);
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

        Region region = regionMapper.toEntity(request);
        region.setCode(request.getCode().toUpperCase().trim());
        region.setName(request.getName().trim());

        region = regionRepository.save(region);
        log.info("Đã tạo mới khu vực: id={}, code={}", region.getId(), region.getCode());
        return mapToResponse(region);
    }

    @Transactional
    public RegionResponse updateRegion(UUID id, UpdateRegionRequest request) {
        Region region = findRegionOrThrow(id);
        regionMapper.updateEntityFromDto(request, region);
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
        RegionResponse res = regionMapper.toResponse(region);
        res.setBranchCount(branchRepository.findAllByRegionId(region.getId()).size());
        return res;
    }
}
