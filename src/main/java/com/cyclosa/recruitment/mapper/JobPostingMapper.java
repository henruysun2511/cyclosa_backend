package com.cyclosa.recruitment.mapper;

import com.cyclosa.recruitment.dto.request.CreateJobPostingRequest;
import com.cyclosa.recruitment.dto.request.UpdateJobPostingRequest;
import com.cyclosa.recruitment.dto.response.JobPostingResponse;
import com.cyclosa.recruitment.entity.JobPosting;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface JobPostingMapper {

    @Mapping(target = "status", ignore = true)
    @Mapping(target = "publishedAt", ignore = true)
    @Mapping(target = "viewCount", ignore = true)
    @Mapping(target = "applyCount", ignore = true)
    JobPosting toEntity(CreateJobPostingRequest request);

    @Mapping(target = "jobPositionId", ignore = true)
    @Mapping(target = "publishedAt", ignore = true)
    @Mapping(target = "viewCount", ignore = true)
    @Mapping(target = "applyCount", ignore = true)
    void updateEntity(@MappingTarget JobPosting entity, UpdateJobPostingRequest request);

    @Mapping(target = "jobPositionTitle", ignore = true)
    JobPostingResponse toResponse(JobPosting entity);
}
