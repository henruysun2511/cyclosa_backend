package com.cyclosa.talent.mapper;

import com.cyclosa.talent.dto.response.*;
import com.cyclosa.talent.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TalentMapper {

    @Mapping(target = "fromPosition", ignore = true)
    @Mapping(target = "toPosition", ignore = true)
    CareerPathResponse toCareerPathResponse(CareerPath careerPath);

    List<CareerPathResponse> toCareerPathResponseList(List<CareerPath> careerPaths);

    @Mapping(target = "position", ignore = true)
    @Mapping(target = "candidateCount", ignore = true)
    SuccessionPlanResponse toSuccessionPlanResponse(SuccessionPlan plan);

    List<SuccessionPlanResponse> toSuccessionPlanResponseList(List<SuccessionPlan> plans);

    @Mapping(target = "position", ignore = true)
    @Mapping(target = "candidates", ignore = true)
    SuccessionPlanDetailResponse toSuccessionPlanDetailResponse(SuccessionPlan plan);

    @Mapping(target = "successionPlanId", source = "successionPlan.id")
    @Mapping(target = "employee", ignore = true)
    SuccessionCandidateResponse toSuccessionCandidateResponse(SuccessionCandidate candidate);

    List<SuccessionCandidateResponse> toSuccessionCandidateResponseList(List<SuccessionCandidate> candidates);

    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "addedByEmployee", ignore = true)
    TalentPoolResponse toTalentPoolResponse(InternalTalentPool talentPool);

    List<TalentPoolResponse> toTalentPoolResponseList(List<InternalTalentPool> list);

    @Mapping(target = "targetPosition", ignore = true)
    CareerSimulationSavedPathResponse toCareerSimulationSavedPathResponse(CareerSimulationSavedPath savedPath);

    List<CareerSimulationSavedPathResponse> toCareerSimulationSavedPathResponseList(List<CareerSimulationSavedPath> list);
}
