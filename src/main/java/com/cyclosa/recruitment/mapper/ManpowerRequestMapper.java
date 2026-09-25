package com.cyclosa.recruitment.mapper;

import com.cyclosa.recruitment.dto.request.CreateManpowerRequest;
import com.cyclosa.recruitment.dto.request.UpdateManpowerRequest;
import com.cyclosa.recruitment.dto.response.ManpowerRequestDetailResponse;
import com.cyclosa.recruitment.dto.response.ManpowerRequestResponse;
import com.cyclosa.recruitment.entity.ManpowerRequest;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ManpowerRequestMapper {

    @Mapping(target = "requestCode", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "workflowInstanceId", ignore = true)
    ManpowerRequest toEntity(CreateManpowerRequest request);

    @Mapping(target = "requestCode", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "workflowInstanceId", ignore = true)
    void updateEntity(@MappingTarget ManpowerRequest entity, UpdateManpowerRequest request);

    @Mapping(target = "department", ignore = true)
    @Mapping(target = "position", ignore = true)
    ManpowerRequestResponse toResponse(ManpowerRequest entity);

    @Mapping(target = "department", ignore = true)
    @Mapping(target = "position", ignore = true)
    ManpowerRequestDetailResponse toDetailResponse(ManpowerRequest entity);
}
