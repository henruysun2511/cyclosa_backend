package com.cyclosa.organization.mapper;

import com.cyclosa.common.dto.summary.JobLevelSummary;
import com.cyclosa.organization.dto.request.CreateJobLevelRequest;
import com.cyclosa.organization.dto.request.CreatePositionRequest;
import com.cyclosa.organization.dto.response.JobLevelResponse;
import com.cyclosa.organization.dto.response.PositionDetailResponse;
import com.cyclosa.organization.dto.response.PositionResponse;
import com.cyclosa.organization.entity.JobLevel;
import com.cyclosa.organization.entity.Position;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PositionMapper {

    @Mapping(target = "companyId", ignore = true)
    @Mapping(target = "status", ignore = true)
    JobLevel toEntity(CreateJobLevelRequest request);

    JobLevelResponse toResponse(JobLevel jobLevel);

    JobLevelSummary toJobLevelSummary(JobLevel jobLevel);

    List<JobLevelResponse> toJobLevelResponseList(List<JobLevel> jobLevels);

    @Mapping(target = "companyId", ignore = true)
    @Mapping(target = "jobLevel", ignore = true)
    @Mapping(target = "status", ignore = true)
    Position toEntity(CreatePositionRequest request);

    @Mapping(target = "company", ignore = true)
    @Mapping(target = "jobLevel", source = "jobLevel")
    PositionResponse toResponse(Position position);

    List<PositionResponse> toPositionResponseList(List<Position> positions);

    @Mapping(target = "company", ignore = true)
    @Mapping(target = "jobLevel", source = "jobLevel")
    PositionDetailResponse toDetailResponse(Position position);
}
