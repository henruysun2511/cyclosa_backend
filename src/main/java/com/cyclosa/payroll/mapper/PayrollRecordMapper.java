package com.cyclosa.payroll.mapper;

import com.cyclosa.payroll.dto.response.PayrollRecordDetailResponse;
import com.cyclosa.payroll.dto.response.PayrollRecordItemResponse;
import com.cyclosa.payroll.dto.response.PayrollRecordResponse;
import com.cyclosa.payroll.dto.response.PayslipResponse;
import com.cyclosa.payroll.entity.PayrollRecord;
import com.cyclosa.payroll.entity.PayrollRecordItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PayrollRecordMapper {

    @Mapping(target = "payrollPeriodId", source = "payrollPeriod.id")
    @Mapping(target = "employee", ignore = true)
    PayrollRecordResponse toResponse(PayrollRecord record);

    @Mapping(target = "payrollPeriodId", source = "payrollPeriod.id")
    @Mapping(target = "payrollPeriodName", source = "payrollPeriod.name")
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "items", source = "items")
    PayrollRecordDetailResponse toDetailResponse(PayrollRecord record);

    List<PayrollRecordResponse> toResponseList(List<PayrollRecord> records);

    PayrollRecordItemResponse toItemResponse(PayrollRecordItem item);

    List<PayrollRecordItemResponse> toItemResponseList(List<PayrollRecordItem> items);

    @Mapping(target = "recordId", source = "id")
    @Mapping(target = "payrollPeriodId", source = "payrollPeriod.id")
    @Mapping(target = "periodName", source = "payrollPeriod.name")
    @Mapping(target = "month", source = "payrollPeriod.month")
    @Mapping(target = "year", source = "payrollPeriod.year")
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "items", source = "items")
    PayslipResponse toPayslipResponse(PayrollRecord record);
}
