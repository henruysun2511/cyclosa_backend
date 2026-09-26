package com.cyclosa.discipline.mapper;

import com.cyclosa.discipline.dto.response.*;
import com.cyclosa.discipline.entity.Discipline;
import com.cyclosa.discipline.entity.Grievance;
import com.cyclosa.discipline.entity.Reward;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DisciplineMapper {

    @Mapping(target = "employeeName", ignore = true)
    @Mapping(target = "employeeCode", ignore = true)
    @Mapping(target = "departmentName", ignore = true)
    @Mapping(target = "decidedByEmployeeName", ignore = true)
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "department", ignore = true)
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "decidedBy", ignore = true)
    RewardResponse toResponse(Reward reward);

    List<RewardResponse> toRewardResponseList(List<Reward> rewards);

    @Mapping(target = "employeeName", ignore = true)
    @Mapping(target = "employeeCode", ignore = true)
    @Mapping(target = "departmentName", ignore = true)
    @Mapping(target = "decidedByEmployeeName", ignore = true)
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "department", ignore = true)
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "decidedBy", ignore = true)
    RewardDetailResponse toDetailResponse(Reward reward);

    @Mapping(target = "employeeName", ignore = true)
    @Mapping(target = "employeeCode", ignore = true)
    @Mapping(target = "departmentName", ignore = true)
    @Mapping(target = "decidedByEmployeeName", ignore = true)
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "department", ignore = true)
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "decidedBy", ignore = true)
    DisciplineResponse toResponse(Discipline discipline);

    List<DisciplineResponse> toDisciplineResponseList(List<Discipline> disciplines);

    @Mapping(target = "employeeName", ignore = true)
    @Mapping(target = "employeeCode", ignore = true)
    @Mapping(target = "departmentName", ignore = true)
    @Mapping(target = "decidedByEmployeeName", ignore = true)
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "department", ignore = true)
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "decidedBy", ignore = true)
    DisciplineDetailResponse toDetailResponse(Discipline discipline);

    @Mapping(target = "employeeName", ignore = true)
    @Mapping(target = "employeeCode", ignore = true)
    @Mapping(target = "departmentName", ignore = true)
    @Mapping(target = "resolvedByEmployeeName", ignore = true)
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "department", ignore = true)
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "resolvedBy", ignore = true)
    GrievanceResponse toResponse(Grievance grievance);

    List<GrievanceResponse> toGrievanceResponseList(List<Grievance> grievances);

    @Mapping(target = "employeeName", ignore = true)
    @Mapping(target = "employeeCode", ignore = true)
    @Mapping(target = "departmentName", ignore = true)
    @Mapping(target = "resolvedByEmployeeName", ignore = true)
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "department", ignore = true)
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "resolvedBy", ignore = true)
    GrievanceDetailResponse toDetailResponse(Grievance grievance);
}
