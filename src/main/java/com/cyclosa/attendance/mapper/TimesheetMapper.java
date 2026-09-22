package com.cyclosa.attendance.mapper;

import com.cyclosa.attendance.dto.response.MonthlyTimesheetResponse;
import com.cyclosa.attendance.entity.MonthlyTimesheet;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TimesheetMapper {

    @Mapping(target = "employee", ignore = true)
    MonthlyTimesheetResponse toResponse(MonthlyTimesheet timesheet);

    List<MonthlyTimesheetResponse> toResponseList(List<MonthlyTimesheet> timesheets);
}
