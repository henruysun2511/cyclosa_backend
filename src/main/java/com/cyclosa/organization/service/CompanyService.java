package com.cyclosa.organization.service;

import com.cyclosa.common.response.PageData;
import com.cyclosa.organization.dto.request.CompanyFilter;
import com.cyclosa.organization.dto.request.CreateCompanyRequest;
import com.cyclosa.organization.dto.request.UpdateCompanyRequest;
import com.cyclosa.organization.dto.response.CompanyResponse;

import java.util.UUID;

public interface CompanyService {

    CompanyResponse createCompany(CreateCompanyRequest request);

    CompanyResponse updateCompany(UUID id, UpdateCompanyRequest request);

    CompanyResponse getCompanyById(UUID id);

    PageData<CompanyResponse> getCompanies(CompanyFilter filter);
}
