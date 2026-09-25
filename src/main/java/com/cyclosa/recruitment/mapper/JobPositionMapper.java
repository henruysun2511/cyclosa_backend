package com.cyclosa.recruitment.mapper;

import com.cyclosa.recruitment.dto.request.CreateJobPositionRequest;
import com.cyclosa.recruitment.dto.request.UpdateJobPositionRequest;
import com.cyclosa.recruitment.dto.response.JobPositionDetailResponse;
import com.cyclosa.recruitment.dto.response.JobPositionResponse;
import com.cyclosa.recruitment.entity.JobPosition;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface JobPositionMapper {

    JobPosition toEntity(CreateJobPositionRequest request);

    void updateEntity(@MappingTarget JobPosition entity, UpdateJobPositionRequest request);

    @Mapping(target = "department", ignore = true)
    @Mapping(target = "position", ignore = true)
    @Mapping(target = "totalApplications", ignore = true)
    JobPositionResponse toResponse(JobPosition entity);

    @Mapping(target = "department", ignore = true)
    @Mapping(target = "position", ignore = true)
    @Mapping(target = "totalApplications", ignore = true)
    JobPositionDetailResponse toDetailResponse(JobPosition entity);
}
