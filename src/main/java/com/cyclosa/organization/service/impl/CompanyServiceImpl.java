package com.cyclosa.organization.service.impl;

import com.cyclosa.common.exception.AppException;
import com.cyclosa.common.response.PageData;
import com.cyclosa.common.util.PageableUtils;
import com.cyclosa.organization.dto.request.CompanyFilter;
import com.cyclosa.organization.dto.request.CreateCompanyRequest;
import com.cyclosa.organization.dto.request.UpdateCompanyRequest;
import com.cyclosa.organization.dto.response.CompanyResponse;
import com.cyclosa.organization.entity.Company;
import com.cyclosa.organization.exception.OrganizationErrorCode;
import com.cyclosa.organization.mapper.CompanyMapper;
import com.cyclosa.organization.repository.CompanyRepository;
import com.cyclosa.organization.service.CompanyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyMapper companyMapper;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("name", "code", "createdAt");

    @Override
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

    @Override
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

    @Override
    @Transactional(readOnly = true)
    public CompanyResponse getCompanyById(UUID id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new AppException(OrganizationErrorCode.COMPANY_NOT_FOUND));
        return companyMapper.toResponse(company);
    }

    @Override
    @Transactional(readOnly = true)
    public PageData<CompanyResponse> getCompanies(CompanyFilter filter) {
        String keyword = PageableUtils.normalizeKeyword(filter.getKeyword());
        Pageable pageable = filter.toPageable("createdAt", ALLOWED_SORT_FIELDS);

        Page<Company> page = companyRepository.search(keyword, filter.getStatus(), pageable);
        return PageData.of(page, companyMapper::toResponse);
    }
}
