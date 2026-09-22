package com.cyclosa.leave.mapper;

import com.cyclosa.leave.dto.request.CreateLeavePolicyRequest;
import com.cyclosa.leave.dto.request.UpdateLeavePolicyRequest;
import com.cyclosa.leave.dto.response.LeavePolicyResponse;
import com.cyclosa.leave.entity.LeavePolicy;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface LeavePolicyMapper {

    @Mapping(target = "companyId", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    LeavePolicy toEntity(CreateLeavePolicyRequest request);

    @Mapping(target = "leaveType", ignore = true)
    LeavePolicyResponse toResponse(LeavePolicy entity);

    List<LeavePolicyResponse> toResponseList(List<LeavePolicy> entities);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "companyId", ignore = true)
    @Mapping(target = "leaveTypeId", ignore = true)
    void updateEntityFromRequest(UpdateLeavePolicyRequest request, @MappingTarget LeavePolicy entity);
}
