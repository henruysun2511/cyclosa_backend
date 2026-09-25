package com.cyclosa.recruitment.mapper;

import com.cyclosa.recruitment.dto.request.CreateCandidateRequest;
import com.cyclosa.recruitment.dto.request.UpdateCandidateRequest;
import com.cyclosa.recruitment.dto.response.CandidateResponse;
import com.cyclosa.recruitment.entity.Candidate;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CandidateMapper {

    Candidate toEntity(CreateCandidateRequest request);

    void updateEntity(@MappingTarget Candidate entity, UpdateCandidateRequest request);

    CandidateResponse toResponse(Candidate entity);
}
