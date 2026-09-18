package com.cyclosa.organization.service;

import com.cyclosa.common.dto.summary.CompanySummary;
import com.cyclosa.common.response.PageData;
import com.cyclosa.organization.dto.request.CompanyFilter;
import com.cyclosa.organization.dto.request.CreateCompanyRequest;
import com.cyclosa.organization.dto.request.UpdateCompanyRequest;
import com.cyclosa.organization.dto.response.CompanyDetailResponse;
import com.cyclosa.organization.dto.response.CompanyResponse;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface CompanyService {

    CompanyResponse createCompany(CreateCompanyRequest request);

    CompanyResponse updateCompany(UUID id, UpdateCompanyRequest request);

    CompanyDetailResponse getCompanyById(UUID id);

    CompanySummary getCompanySummary(UUID id);

    Map<UUID, CompanySummary> getCompanySummaries(Set<UUID> ids);

    PageData<CompanyResponse> getCompanies(CompanyFilter filter);
}
