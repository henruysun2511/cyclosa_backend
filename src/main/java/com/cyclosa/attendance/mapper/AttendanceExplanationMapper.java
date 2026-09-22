package com.cyclosa.attendance.mapper;

import com.cyclosa.attendance.dto.response.AttendanceExplanationResponse;
import com.cyclosa.attendance.entity.AttendanceExplanation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AttendanceExplanationMapper {

    @Mapping(target = "employee", ignore = true)
    AttendanceExplanationResponse toResponse(AttendanceExplanation explanation);

    @Mapping(target = "company", ignore = true)
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "attendanceRecord", ignore = true)
    @Mapping(target = "workflowHistory", ignore = true)
    com.cyclosa.attendance.dto.response.AttendanceExplanationDetailResponse toDetailResponse(AttendanceExplanation explanation);

    List<AttendanceExplanationResponse> toResponseList(List<AttendanceExplanation> explanations);
}
