package com.cyclosa.payroll.mapper;

import com.cyclosa.payroll.dto.request.CreateSalaryAdvanceRequest;
import com.cyclosa.payroll.dto.response.SalaryAdvanceDetailResponse;
import com.cyclosa.payroll.dto.response.SalaryAdvanceResponse;
import com.cyclosa.payroll.entity.SalaryAdvance;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SalaryAdvanceMapper {

    @Mapping(target = "companyId", ignore = true)
    @Mapping(target = "employeeId", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "workflowInstanceId", ignore = true)
    @Mapping(target = "disbursedAt", ignore = true)
    @Mapping(target = "deductedPayrollPeriodId", ignore = true)
    SalaryAdvance toEntity(CreateSalaryAdvanceRequest request);

    @Mapping(target = "employee", ignore = true)
    SalaryAdvanceResponse toResponse(SalaryAdvance advance);

    @Mapping(target = "company", ignore = true)
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "workflowHistory", ignore = true)
    SalaryAdvanceDetailResponse toDetailResponse(SalaryAdvance advance);

    List<SalaryAdvanceResponse> toResponseList(List<SalaryAdvance> advances);
}
