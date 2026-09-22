package com.cyclosa.payroll.mapper;

import com.cyclosa.payroll.dto.request.CreateSalaryComponentRequest;
import com.cyclosa.payroll.dto.request.UpdateSalaryComponentRequest;
import com.cyclosa.payroll.dto.response.SalaryComponentDetailResponse;
import com.cyclosa.payroll.dto.response.SalaryComponentResponse;
import com.cyclosa.payroll.entity.SalaryComponent;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SalaryComponentMapper {

    @Mapping(target = "companyId", ignore = true)
    SalaryComponent toEntity(CreateSalaryComponentRequest request);

    SalaryComponentResponse toResponse(SalaryComponent component);

    @Mapping(target = "company", ignore = true)
    SalaryComponentDetailResponse toDetailResponse(SalaryComponent component);

    List<SalaryComponentResponse> toResponseList(List<SalaryComponent> components);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "companyId", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateEntityFromRequest(UpdateSalaryComponentRequest request, @MappingTarget SalaryComponent component);
}
