package com.cyclosa.organization.service;

import com.cyclosa.common.response.PageData;
import com.cyclosa.organization.dto.request.CreateJobLevelRequest;
import com.cyclosa.organization.dto.request.CreatePositionRequest;
import com.cyclosa.organization.dto.request.PositionFilter;
import com.cyclosa.organization.dto.request.UpdatePositionRequest;
import com.cyclosa.organization.dto.response.JobLevelResponse;
import com.cyclosa.organization.dto.response.PositionDetailResponse;
import com.cyclosa.organization.dto.response.PositionResponse;

import java.util.List;
import java.util.UUID;

public interface PositionService {

    JobLevelResponse createJobLevel(UUID companyId, CreateJobLevelRequest request);

    List<JobLevelResponse> getJobLevels(UUID companyId);

    PositionResponse createPosition(UUID companyId, CreatePositionRequest request);

    PositionResponse updatePosition(UUID companyId, UUID id, UpdatePositionRequest request);

    PositionDetailResponse getPositionById(UUID companyId, UUID id);

    PageData<PositionResponse> getPositions(UUID companyId, PositionFilter filter);
}
