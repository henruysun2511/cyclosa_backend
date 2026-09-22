package com.cyclosa.attendance.mapper;

import com.cyclosa.attendance.dto.response.ShiftAssignmentResponse;
import com.cyclosa.attendance.entity.ShiftAssignment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {ShiftMapper.class})
public interface ShiftAssignmentMapper {

    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "shift", source = "shift")
    ShiftAssignmentResponse toResponse(ShiftAssignment assignment);

    List<ShiftAssignmentResponse> toResponseList(List<ShiftAssignment> assignments);
}
