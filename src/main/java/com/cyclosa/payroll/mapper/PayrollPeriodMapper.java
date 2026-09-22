package com.cyclosa.payroll.mapper;

import com.cyclosa.payroll.dto.request.CreatePayrollPeriodRequest;
import com.cyclosa.payroll.dto.request.UpdatePayrollPeriodRequest;
import com.cyclosa.payroll.dto.response.PayrollPeriodDetailResponse;
import com.cyclosa.payroll.dto.response.PayrollPeriodResponse;
import com.cyclosa.payroll.entity.PayrollPeriod;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PayrollPeriodMapper {

    @Mapping(target = "companyId", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "totalGross", ignore = true)
    @Mapping(target = "totalNet", ignore = true)
    @Mapping(target = "workflowInstanceId", ignore = true)
    @Mapping(target = "version", ignore = true)
    PayrollPeriod toEntity(CreatePayrollPeriodRequest request);

    PayrollPeriodResponse toResponse(PayrollPeriod period);

    @Mapping(target = "company", ignore = true)
    @Mapping(target = "totalRecordsCount", ignore = true)
    @Mapping(target = "workflowHistory", ignore = true)
    PayrollPeriodDetailResponse toDetailResponse(PayrollPeriod period);

    List<PayrollPeriodResponse> toResponseList(List<PayrollPeriod> periods);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "companyId", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "month", ignore = true)
    @Mapping(target = "year", ignore = true)
    @Mapping(target = "startDate", ignore = true)
    @Mapping(target = "endDate", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "totalGross", ignore = true)
    @Mapping(target = "totalNet", ignore = true)
    @Mapping(target = "workflowInstanceId", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateEntityFromRequest(UpdatePayrollPeriodRequest request, @MappingTarget PayrollPeriod period);
}
