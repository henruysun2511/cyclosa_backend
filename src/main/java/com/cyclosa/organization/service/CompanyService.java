package com.cyclosa.organization.service;

import com.cyclosa.common.exception.AppException;
import com.cyclosa.common.response.PageData;
import com.cyclosa.organization.dto.request.CompanyFilterRequest;
import com.cyclosa.organization.dto.request.CreateCompanyRequest;
import com.cyclosa.organization.dto.request.UpdateCompanyRequest;
import com.cyclosa.organization.dto.response.CompanyResponse;
import com.cyclosa.organization.entity.Company;
import com.cyclosa.organization.exception.OrganizationErrorCode;
import com.cyclosa.organization.repository.CompanyRepository;
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
@Transactional(readOnly = true)
public class CompanyService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("code", "name", "status", "createdAt");

    private final CompanyRepository companyRepository;

    public PageData<CompanyResponse> getCompanies(CompanyFilterRequest filter) {
        Pageable pageable = filter.toPageable("createdAt", ALLOWED_SORT_FIELDS);
        Page<Company> page = companyRepository.search(filter.getKeyword(), filter.getStatus(), pageable);
        return PageData.of(page, this::mapToResponse);
    }

    public CompanyResponse getCompanyById(UUID id) {
        Company company = findCompanyOrThrow(id);
        return mapToResponse(company);
    }

    @Transactional
    public CompanyResponse createCompany(CreateCompanyRequest request) {
        if (companyRepository.existsByCode(request.getCode())) {
            throw new AppException(OrganizationErrorCode.COMPANY_CODE_EXISTS);
        }

        Company company = Company.builder()
                .code(request.getCode().toUpperCase().trim())
                .name(request.getName().trim())
                .taxCode(request.getTaxCode())
                .address(request.getAddress())
                .phone(request.getPhone())
                .email(request.getEmail())
                .logoUrl(request.getLogoUrl())
                .status("ACTIVE")
                .deleted(false)
                .build();

        company = companyRepository.save(company);
        log.info("Đã tạo mới công ty: id={}, code={}", company.getId(), company.getCode());
        return mapToResponse(company);
    }

    @Transactional
    public CompanyResponse updateCompany(UUID id, UpdateCompanyRequest request) {
        Company company = findCompanyOrThrow(id);

        company.setName(request.getName().trim());
        company.setTaxCode(request.getTaxCode());
        company.setAddress(request.getAddress());
        company.setPhone(request.getPhone());
        company.setEmail(request.getEmail());
        company.setLogoUrl(request.getLogoUrl());
        if (request.getStatus() != null) {
            company.setStatus(request.getStatus());
        }

        company = companyRepository.save(company);
        log.info("Đã cập nhật công ty: id={}", company.getId());
        return mapToResponse(company);
    }

    @Transactional
    public void deleteCompany(UUID id) {
        Company company = findCompanyOrThrow(id);
        companyRepository.delete(company);
        log.info("Đã xóa mềm công ty: id={}", id);
    }

    public Company findCompanyOrThrow(UUID id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new AppException(OrganizationErrorCode.COMPANY_NOT_FOUND));
    }

    private CompanyResponse mapToResponse(Company company) {
        return CompanyResponse.builder()
                .id(company.getId())
                .code(company.getCode())
                .name(company.getName())
                .taxCode(company.getTaxCode())
                .address(company.getAddress())
                .phone(company.getPhone())
                .email(company.getEmail())
                .logoUrl(company.getLogoUrl())
                .status(company.getStatus())
                .createdAt(company.getCreatedAt())
                .updatedAt(company.getUpdatedAt())
                .build();
    }
}

