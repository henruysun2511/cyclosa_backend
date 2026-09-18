package com.cyclosa.organization.service.impl;

import com.cyclosa.common.dto.summary.CompanySummary;
import com.cyclosa.common.exception.AppException;
import com.cyclosa.organization.dto.request.CreateBranchRequest;
import com.cyclosa.organization.dto.request.CreateRegionRequest;
import com.cyclosa.organization.dto.request.UpdateBranchRequest;
import com.cyclosa.organization.dto.response.BranchDetailResponse;
import com.cyclosa.organization.dto.response.BranchResponse;
import com.cyclosa.organization.dto.response.RegionResponse;
import com.cyclosa.organization.entity.Branch;
import com.cyclosa.organization.entity.Region;
import com.cyclosa.organization.exception.OrganizationErrorCode;
import com.cyclosa.organization.mapper.GeographyMapper;
import com.cyclosa.organization.repository.BranchRepository;
import com.cyclosa.organization.repository.RegionRepository;
import com.cyclosa.organization.service.CompanyService;
import com.cyclosa.organization.service.GeographyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeographyServiceImpl implements GeographyService {

    private final RegionRepository regionRepository;
    private final BranchRepository branchRepository;
    private final GeographyMapper geographyMapper;
    private final CompanyService companyService;

    @Override
    @Transactional
    public RegionResponse createRegion(UUID companyId, CreateRegionRequest request) {
        UUID effectiveCompanyId = request.getCompanyId() != null ? request.getCompanyId() : companyId;
        if (effectiveCompanyId == null) {
            throw AppException.badRequest("Yêu cầu cung cấp ID công ty");
        }

        if (regionRepository.existsByCodeAndCompanyId(request.getCode(), effectiveCompanyId)) {
            throw AppException.conflict("Mã vùng miền đã tồn tại trong công ty");
        }

        Region region = geographyMapper.toEntity(request);
        region.setCompanyId(effectiveCompanyId);
        region = regionRepository.save(region);
        log.info("Created Region id={}, code={}", region.getId(), region.getCode());
        return geographyMapper.toResponse(region);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RegionResponse> getGeographyTree(UUID companyId) {
        if (companyId == null) {
            throw AppException.badRequest("Yêu cầu cung cấp ID công ty");
        }
        List<Region> regions = regionRepository.findAllByCompanyIdWithBranches(companyId);
        CompanySummary companySummary = companyService.getCompanySummary(companyId);

        List<RegionResponse> responses = geographyMapper.toRegionResponseList(regions);
        for (RegionResponse r : responses) {
            if (r.getBranches() != null) {
                r.getBranches().forEach(b -> b.setCompany(companySummary));
            }
        }
        return responses;
    }

    @Override
    @Transactional
    public BranchResponse createBranch(UUID companyId, CreateBranchRequest request) {
        UUID effectiveCompanyId = request.getCompanyId() != null ? request.getCompanyId() : companyId;
        if (effectiveCompanyId == null) {
            throw AppException.badRequest("Yêu cầu cung cấp ID công ty");
        }

        Region region = regionRepository.findById(request.getRegionId())
                .orElseThrow(() -> new AppException(OrganizationErrorCode.REGION_NOT_FOUND));

        if (branchRepository.existsByCodeAndCompanyId(request.getCode(), effectiveCompanyId)) {
            throw new AppException(OrganizationErrorCode.BRANCH_CODE_EXISTS);
        }

        Branch branch = geographyMapper.toEntity(request);
        branch.setCompanyId(effectiveCompanyId);
        branch.setRegion(region);
        branch = branchRepository.save(branch);
        log.info("Created Branch id={}, code={}", branch.getId(), branch.getCode());

        BranchResponse response = geographyMapper.toResponse(branch);
        response.setCompany(companyService.getCompanySummary(effectiveCompanyId));
        return response;
    }

    @Override
    @Transactional
    public BranchResponse updateBranch(UUID companyId, UUID id, UpdateBranchRequest request) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new AppException(OrganizationErrorCode.BRANCH_NOT_FOUND));

        if (companyId != null && !branch.getCompanyId().equals(companyId)) {
            throw new AppException(OrganizationErrorCode.BRANCH_NOT_FOUND);
        }

        Region region = regionRepository.findById(request.getRegionId())
                .orElseThrow(() -> new AppException(OrganizationErrorCode.REGION_NOT_FOUND));

        branch.setRegion(region);
        branch.setName(request.getName());
        branch.setAddress(request.getAddress());
        branch.setLatitude(request.getLatitude());
        branch.setLongitude(request.getLongitude());
        if (request.getCheckinRadiusMeters() != null) {
            branch.setCheckinRadiusMeters(request.getCheckinRadiusMeters());
        }
        branch.setStatus(request.getStatus());

        branch = branchRepository.save(branch);
        log.info("Updated Branch id={}", branch.getId());

        BranchResponse response = geographyMapper.toResponse(branch);
        response.setCompany(companyService.getCompanySummary(branch.getCompanyId()));
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public BranchDetailResponse getBranchById(UUID companyId, UUID id) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new AppException(OrganizationErrorCode.BRANCH_NOT_FOUND));

        if (companyId != null && !branch.getCompanyId().equals(companyId)) {
            throw new AppException(OrganizationErrorCode.BRANCH_NOT_FOUND);
        }

        BranchDetailResponse response = geographyMapper.toDetailResponse(branch);
        response.setCompany(companyService.getCompanySummary(branch.getCompanyId()));
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BranchResponse> getBranches(UUID companyId) {
        if (companyId == null) {
            throw AppException.badRequest("Yêu cầu cung cấp ID công ty");
        }
        List<Branch> branches = branchRepository.findByCompanyIdWithRegion(companyId);
        CompanySummary companySummary = companyService.getCompanySummary(companyId);

        return branches.stream().map(b -> {
            BranchResponse res = geographyMapper.toResponse(b);
            res.setCompany(companySummary);
            return res;
        }).toList();
    }
}
