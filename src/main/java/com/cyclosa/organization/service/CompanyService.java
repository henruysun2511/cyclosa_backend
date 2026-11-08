package com.cyclosa.organization.service;

import com.cyclosa.common.exception.AppException;
import com.cyclosa.common.response.PageData;
import com.cyclosa.common.util.PageableUtils;
import com.cyclosa.organization.dto.request.CompanyFilterRequest;
import com.cyclosa.organization.dto.request.CreateCompanyRequest;
import com.cyclosa.organization.dto.request.UpdateCompanyRequest;
import com.cyclosa.organization.dto.response.CompanyResponse;
import com.cyclosa.organization.entity.Company;
import com.cyclosa.organization.exception.OrganizationErrorCode;
import com.cyclosa.organization.mapper.CompanyMapper;
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
    private final CompanyMapper companyMapper;

    public PageData<CompanyResponse> getCompanies(CompanyFilterRequest filter) {
        String kw = PageableUtils.normalizeKeyword(filter.getKeyword());
        Pageable pageable = filter.toPageable("createdAt", ALLOWED_SORT_FIELDS);
        Page<Company> page;
        if (kw == null && (filter.getStatus() == null || filter.getStatus().isBlank())) {
            page = companyRepository.findAll(pageable);
        } else {
            page = companyRepository.search(kw, filter.getStatus(), pageable);
        }
        return PageData.of(page, companyMapper::toResponse);
    }

    public CompanyResponse getCompanyById(UUID id) {
        Company company = findCompanyOrThrow(id);
        return companyMapper.toResponse(company);
    }

    @Transactional
    public CompanyResponse createCompany(CreateCompanyRequest request) {
        if (companyRepository.existsByCode(request.getCode())) {
            throw new AppException(OrganizationErrorCode.COMPANY_CODE_EXISTS);
        }

        Company company = companyMapper.toEntity(request);
        company.setCode(request.getCode().toUpperCase().trim());
        company.setName(request.getName().trim());

        company = companyRepository.save(company);
        log.info("Đã tạo mới công ty: id={}, code={}", company.getId(), company.getCode());
        return companyMapper.toResponse(company);
    }

    @Transactional
    public CompanyResponse updateCompany(UUID id, UpdateCompanyRequest request) {
        Company company = findCompanyOrThrow(id);
        companyMapper.updateEntityFromDto(request, company);
        company.setName(request.getName().trim());

        company = companyRepository.save(company);
        log.info("Đã cập nhật công ty: id={}", company.getId());
        return companyMapper.toResponse(company);
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
}
