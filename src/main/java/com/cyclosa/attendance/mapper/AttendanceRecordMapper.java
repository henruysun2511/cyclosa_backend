package com.cyclosa.attendance.mapper;

import com.cyclosa.attendance.dto.response.AttendanceRecordResponse;
import com.cyclosa.attendance.dto.response.AttendanceTodayResponse;
import com.cyclosa.attendance.entity.AttendanceRecord;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AttendanceRecordMapper {

    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "branch", ignore = true)
    @Mapping(target = "shift", ignore = true)
    AttendanceRecordResponse toResponse(AttendanceRecord record);

    List<AttendanceRecordResponse> toResponseList(List<AttendanceRecord> records);

    @Mapping(target = "shift", ignore = true)
    @Mapping(target = "branch", ignore = true)
    @Mapping(target = "hasCheckedIn", expression = "java(record.getCheckInTime() != null)")
    @Mapping(target = "hasCheckedOut", expression = "java(record.getCheckOutTime() != null)")
    AttendanceTodayResponse toTodayResponse(AttendanceRecord record);
}
