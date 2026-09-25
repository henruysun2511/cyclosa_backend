package com.cyclosa.recruitment.mapper;

import com.cyclosa.recruitment.dto.response.ApplicationDetailResponse;
import com.cyclosa.recruitment.dto.response.ApplicationResponse;
import com.cyclosa.recruitment.entity.Application;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ApplicationMapper {
    @Mapping(target = "candidateName", ignore = true)
    @Mapping(target = "candidateEmail", ignore = true)
    @Mapping(target = "candidatePhone", ignore = true)
    @Mapping(target = "jobPositionTitle", ignore = true)
    ApplicationResponse toResponse(Application entity);

    @Mapping(target = "candidateName", ignore = true)
    @Mapping(target = "candidateEmail", ignore = true)
    @Mapping(target = "candidatePhone", ignore = true)
    @Mapping(target = "jobPositionTitle", ignore = true)
    ApplicationDetailResponse toDetailResponse(Application entity);
}
