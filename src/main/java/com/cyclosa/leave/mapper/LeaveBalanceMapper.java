package com.cyclosa.leave.mapper;

import com.cyclosa.leave.dto.response.LeaveBalanceResponse;
import com.cyclosa.leave.entity.LeaveBalance;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface LeaveBalanceMapper {

    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "leaveType", ignore = true)
    @Mapping(target = "availableDays", expression = "java(entity.getAvailableDays())")
    @Mapping(target = "remainingDays", expression = "java(entity.getRemainingDays())")
    LeaveBalanceResponse toResponse(LeaveBalance entity);

    List<LeaveBalanceResponse> toResponseList(List<LeaveBalance> entities);
}
