package com.cyclosa.leave.mapper;

import com.cyclosa.leave.dto.request.CreateLeaveTypeRequest;
import com.cyclosa.leave.dto.request.UpdateLeaveTypeRequest;
import com.cyclosa.leave.dto.response.LeaveTypeResponse;
import com.cyclosa.leave.entity.LeaveType;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface LeaveTypeMapper {

    @Mapping(target = "companyId", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    LeaveType toEntity(CreateLeaveTypeRequest request);

    LeaveTypeResponse toResponse(LeaveType entity);

    List<LeaveTypeResponse> toResponseList(List<LeaveType> entities);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "companyId", ignore = true)
    @Mapping(target = "code", ignore = true)
    void updateEntityFromRequest(UpdateLeaveTypeRequest request, @MappingTarget LeaveType entity);
}
