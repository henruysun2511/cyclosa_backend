package com.cyclosa.organization.mapper;

import com.cyclosa.organization.dto.request.CreateCompanyRequest;
import com.cyclosa.organization.dto.request.UpdateCompanyRequest;
import com.cyclosa.organization.dto.response.CompanyResponse;
import com.cyclosa.organization.entity.Company;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        builder = @Builder(disableBuilder = true),
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface CompanyMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", constant = "ACTIVE")
    Company toEntity(CreateCompanyRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true)
    void updateEntityFromDto(UpdateCompanyRequest request, @MappingTarget Company company);

    CompanyResponse toResponse(Company company);

    List<CompanyResponse> toResponseList(List<Company> companies);
}

