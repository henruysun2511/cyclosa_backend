package com.cyclosa.organization.mapper;

import com.cyclosa.organization.dto.request.CreateJobTitleRequest;
import com.cyclosa.organization.dto.request.UpdateJobTitleRequest;
import com.cyclosa.organization.dto.response.JobTitleResponse;
import com.cyclosa.organization.entity.JobTitle;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        builder = @Builder(disableBuilder = true),
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface JobTitleMapper {

    @Mapping(target = "id", ignore = true)
    JobTitle toEntity(CreateJobTitleRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "companyId", ignore = true)
    @Mapping(target = "code", ignore = true)
    void updateEntityFromDto(UpdateJobTitleRequest request, @MappingTarget JobTitle jobTitle);

    JobTitleResponse toResponse(JobTitle jobTitle);

    List<JobTitleResponse> toResponseList(List<JobTitle> jobTitles);
}

