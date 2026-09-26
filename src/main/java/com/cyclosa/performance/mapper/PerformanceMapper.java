package com.cyclosa.performance.mapper;

import com.cyclosa.performance.dto.request.CreateGoalRequest;
import com.cyclosa.performance.dto.request.CreateKpiRequest;
import com.cyclosa.performance.dto.request.CreatePerformanceCycleRequest;
import com.cyclosa.performance.dto.response.*;
import com.cyclosa.performance.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PerformanceMapper {

    PerformanceCycle toEntity(CreatePerformanceCycleRequest request);

    PerformanceCycleResponse toResponse(PerformanceCycle cycle);

    List<PerformanceCycleResponse> toCycleResponseList(List<PerformanceCycle> cycles);

    Kpi toEntity(CreateKpiRequest request);

    @Mapping(target = "organizationalUnit", ignore = true)
    @Mapping(target = "company", ignore = true)
    KpiResponse toResponse(Kpi kpi);

    List<KpiResponse> toKpiResponseList(List<Kpi> kpis);

    Goal toEntity(CreateGoalRequest request);

    @Mapping(target = "employee", ignore = true)
    GoalResponse toResponse(Goal goal);

    List<GoalResponse> toGoalResponseList(List<Goal> goals);

    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "department", ignore = true)
    @Mapping(target = "finalizedBy", ignore = true)
    PerformanceEvaluationResponse toResponse(PerformanceEvaluation evaluation);

    List<PerformanceEvaluationResponse> toEvaluationResponseList(List<PerformanceEvaluation> evaluations);

    PerformanceReviewResponse toResponse(PerformanceReview review);
}
