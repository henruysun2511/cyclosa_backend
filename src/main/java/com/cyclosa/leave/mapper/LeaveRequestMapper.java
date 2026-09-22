package com.cyclosa.leave.mapper;

import com.cyclosa.leave.dto.request.CreateLeaveRequestRequest;
import com.cyclosa.leave.dto.response.LeaveRequestDetailResponse;
import com.cyclosa.leave.dto.response.LeaveRequestResponse;
import com.cyclosa.leave.entity.LeaveRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface LeaveRequestMapper {

    @Mapping(target = "companyId", ignore = true)
    @Mapping(target = "employeeId", ignore = true)
    @Mapping(target = "totalDays", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "workflowInstanceId", ignore = true)
    @Mapping(target = "rejectionReason", ignore = true)
    @Mapping(target = "cancelledAt", ignore = true)
    @Mapping(target = "cancelledBy", ignore = true)
    @Mapping(target = "approvedAt", ignore = true)
    LeaveRequest toEntity(CreateLeaveRequestRequest request);

    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "leaveType", ignore = true)
    LeaveRequestResponse toResponse(LeaveRequest entity);

    @Mapping(target = "company", ignore = true)
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "leaveType", ignore = true)
    @Mapping(target = "workflowHistory", ignore = true)
    LeaveRequestDetailResponse toDetailResponse(LeaveRequest entity);

    List<LeaveRequestResponse> toResponseList(List<LeaveRequest> entities);
}
