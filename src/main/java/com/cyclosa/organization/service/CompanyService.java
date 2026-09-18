package com.cyclosa.organization.service;

import com.cyclosa.common.dto.summary.CompanySummary;
import com.cyclosa.common.exception.AppException;
import com.cyclosa.common.response.PageData;
import com.cyclosa.common.util.PageableUtils;
import com.cyclosa.organization.dto.request.CompanyFilter;
import com.cyclosa.organization.dto.request.CreateCompanyRequest;
import com.cyclosa.organization.dto.request.UpdateCompanyRequest;
import com.cyclosa.organization.dto.response.CompanyDetailResponse;
import com.cyclosa.organization.dto.response.CompanyResponse;
import com.cyclosa.organization.entity.Company;
import com.cyclosa.organization.exception.OrganizationErrorCode;
import com.cyclosa.organization.mapper.CompanyMapper;
import com.cyclosa.organization.repository.BranchRepository;
import com.cyclosa.organization.repository.CompanyRepository;
import com.cyclosa.organization.repository.OrganizationalUnitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final BranchRepository branchRepository;
    private final OrganizationalUnitRepository orgUnitRepository;
    private final CompanyMapper companyMapper;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("name", "code", "createdAt");
    @Transactional
    public CompanyResponse createCompany(CreateCompanyRequest request) {
        if (companyRepository.existsByCode(request.getCode())) {
            throw new AppException(OrganizationErrorCode.COMPANY_CODE_EXISTS);
        }

        Company company = companyMapper.toEntity(request);
        company = companyRepository.save(company);
        log.info("Created company id={}, code={}", company.getId(), company.getCode());
        return companyMapper.toResponse(company);
    }
    @Transactional
    public CompanyResponse updateCompany(UUID id, UpdateCompanyRequest request) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new AppException(OrganizationErrorCode.COMPANY_NOT_FOUND));

        company.setName(request.getName());
        company.setTaxCode(request.getTaxCode());
        company.setEmail(request.getEmail());
        company.setPhone(request.getPhone());
        company.setAddress(request.getAddress());
        company.setLogoUrl(request.getLogoUrl());
        company.setStatus(request.getStatus());

        company = companyRepository.save(company);
        log.info("Updated company id={}", company.getId());
        return companyMapper.toResponse(company);
    }
    @Transactional(readOnly = true)
    public CompanyDetailResponse getCompanyById(UUID id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new AppException(OrganizationErrorCode.COMPANY_NOT_FOUND));
        CompanyDetailResponse response = companyMapper.toDetailResponse(company);
        response.setTotalBranches(branchRepository.countByCompanyId(id));
        response.setTotalUnits(orgUnitRepository.countByCompanyId(id));
        return response;
    }
    @Transactional(readOnly = true)
    public CompanySummary getCompanySummary(UUID id) {
        if (id == null) return null;
        return companyRepository.findById(id)
                .map(companyMapper::toSummary)
                .orElse(null);
    }
    @Transactional(readOnly = true)
    public Map<UUID, CompanySummary> getCompanySummaries(Set<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyMap();
        }
        return companyRepository.findAllById(ids).stream()
                .map(companyMapper::toSummary)
                .collect(Collectors.toMap(CompanySummary::getId, s -> s));
    }
    @Transactional(readOnly = true)
    public PageData<CompanyResponse> getCompanies(CompanyFilter filter) {
        String keyword = PageableUtils.normalizeKeyword(filter.getKeyword());
        Pageable pageable = filter.toPageable("createdAt", ALLOWED_SORT_FIELDS);

        Page<Company> page = companyRepository.search(keyword, filter.getStatus(), pageable);
        return PageData.of(page, companyMapper::toResponse);
    }
}
