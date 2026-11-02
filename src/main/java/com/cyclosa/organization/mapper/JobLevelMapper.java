package com.cyclosa.organization.mapper;

import com.cyclosa.organization.dto.request.CreateJobLevelRequest;
import com.cyclosa.organization.dto.request.UpdateJobLevelRequest;
import com.cyclosa.organization.dto.response.JobLevelResponse;
import com.cyclosa.organization.entity.JobLevel;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        builder = @Builder(disableBuilder = true),
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface JobLevelMapper {

    @Mapping(target = "id", ignore = true)
    JobLevel toEntity(CreateJobLevelRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "companyId", ignore = true)
    @Mapping(target = "code", ignore = true)
    void updateEntityFromDto(UpdateJobLevelRequest request, @MappingTarget JobLevel jobLevel);

    JobLevelResponse toResponse(JobLevel jobLevel);

    List<JobLevelResponse> toResponseList(List<JobLevel> jobLevels);
}

