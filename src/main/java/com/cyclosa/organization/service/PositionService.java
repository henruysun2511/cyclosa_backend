package com.cyclosa.organization.service;

import com.cyclosa.common.dto.summary.CompanySummary;
import com.cyclosa.common.dto.summary.JobLevelSummary;
import com.cyclosa.common.dto.summary.PositionSummary;
import com.cyclosa.common.exception.AppException;
import com.cyclosa.common.response.PageData;
import com.cyclosa.common.util.PageableUtils;
import com.cyclosa.organization.dto.request.CreateJobLevelRequest;
import com.cyclosa.organization.dto.request.CreatePositionRequest;
import com.cyclosa.organization.dto.request.PositionFilter;
import com.cyclosa.organization.dto.request.UpdatePositionRequest;
import com.cyclosa.organization.dto.response.JobLevelResponse;
import com.cyclosa.organization.dto.response.PositionDetailResponse;
import com.cyclosa.organization.dto.response.PositionResponse;
import com.cyclosa.organization.entity.JobLevel;
import com.cyclosa.organization.entity.Position;
import com.cyclosa.organization.exception.OrganizationErrorCode;
import com.cyclosa.organization.mapper.PositionMapper;
import com.cyclosa.organization.repository.JobLevelRepository;
import com.cyclosa.organization.repository.PositionRepository;
import com.cyclosa.organization.service.CompanyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PositionService {

    private final JobLevelRepository jobLevelRepository;
    private final PositionRepository positionRepository;
    private final PositionMapper positionMapper;
    private final CompanyService companyService;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("name", "code", "createdAt");
    @Transactional
    public JobLevelResponse createJobLevel(UUID companyId, CreateJobLevelRequest request) {
        UUID effectiveCompanyId = request.getCompanyId() != null ? request.getCompanyId() : companyId;
        if (effectiveCompanyId == null) {
            throw AppException.badRequest("Yêu cầu cung cấp ID công ty");
        }

        if (jobLevelRepository.existsByCodeAndCompanyId(request.getCode(), effectiveCompanyId)) {
            throw new AppException(OrganizationErrorCode.JOB_LEVEL_CODE_EXISTS);
        }

        JobLevel level = positionMapper.toEntity(request);
        level.setCompanyId(effectiveCompanyId);
        level = jobLevelRepository.save(level);
        log.info("Created JobLevel id={}, code={}", level.getId(), level.getCode());
        return positionMapper.toResponse(level);
    }
    @Transactional(readOnly = true)
    public List<JobLevelResponse> getJobLevels(UUID companyId) {
        if (companyId == null) {
            throw AppException.badRequest("Yêu cầu cung cấp ID công ty");
        }
        List<JobLevel> levels = jobLevelRepository.findByCompanyIdOrderByRankOrderAsc(companyId);
        return positionMapper.toJobLevelResponseList(levels);
    }
    @Transactional
    public PositionResponse createPosition(UUID companyId, CreatePositionRequest request) {
        UUID effectiveCompanyId = request.getCompanyId() != null ? request.getCompanyId() : companyId;
        if (effectiveCompanyId == null) {
            throw AppException.badRequest("Yêu cầu cung cấp ID công ty");
        }

        if (positionRepository.existsByCodeAndCompanyId(request.getCode(), effectiveCompanyId)) {
            throw new AppException(OrganizationErrorCode.POSITION_CODE_EXISTS);
        }

        Position position = positionMapper.toEntity(request);
        position.setCompanyId(effectiveCompanyId);

        if (request.getJobLevelId() != null) {
            JobLevel level = jobLevelRepository.findById(request.getJobLevelId())
                    .orElseThrow(() -> new AppException(OrganizationErrorCode.JOB_LEVEL_NOT_FOUND));
            position.setJobLevel(level);
        }

        position = positionRepository.save(position);
        log.info("Created Position id={}, code={}", position.getId(), position.getCode());

        PositionResponse response = positionMapper.toResponse(position);
        response.setCompany(companyService.getCompanySummary(effectiveCompanyId));
        return response;
    }
    @Transactional
    public PositionResponse updatePosition(UUID companyId, UUID id, UpdatePositionRequest request) {
        Position position = positionRepository.findById(id)
                .orElseThrow(() -> new AppException(OrganizationErrorCode.POSITION_NOT_FOUND));

        if (companyId != null && !position.getCompanyId().equals(companyId)) {
            throw new AppException(OrganizationErrorCode.POSITION_NOT_FOUND);
        }

        position.setName(request.getName());
        position.setDescription(request.getDescription());
        position.setStatus(request.getStatus());

        if (request.getJobLevelId() != null) {
            JobLevel level = jobLevelRepository.findById(request.getJobLevelId())
                    .orElseThrow(() -> new AppException(OrganizationErrorCode.JOB_LEVEL_NOT_FOUND));
            position.setJobLevel(level);
        } else {
            position.setJobLevel(null);
        }

        position = positionRepository.save(position);
        log.info("Updated Position id={}", position.getId());

        PositionResponse response = positionMapper.toResponse(position);
        response.setCompany(companyService.getCompanySummary(position.getCompanyId()));
        return response;
    }
    @Transactional(readOnly = true)
    public PositionDetailResponse getPositionById(UUID companyId, UUID id) {
        Position position = positionRepository.findById(id)
                .orElseThrow(() -> new AppException(OrganizationErrorCode.POSITION_NOT_FOUND));

        if (companyId != null && !position.getCompanyId().equals(companyId)) {
            throw new AppException(OrganizationErrorCode.POSITION_NOT_FOUND);
        }

        PositionDetailResponse response = positionMapper.toDetailResponse(position);
        response.setCompany(companyService.getCompanySummary(position.getCompanyId()));
        return response;
    }
    @Transactional(readOnly = true)
    public PageData<PositionResponse> getPositions(UUID companyId, PositionFilter filter) {
        if (companyId == null) {
            throw AppException.badRequest("Yêu cầu cung cấp ID công ty");
        }

        String keyword = PageableUtils.normalizeKeyword(filter.getKeyword());
        Pageable pageable = filter.toPageable("createdAt", ALLOWED_SORT_FIELDS);

        Page<Position> page = positionRepository.search(keyword, companyId, filter.getJobLevelId(), pageable);
        CompanySummary companySummary = companyService.getCompanySummary(companyId);

        List<PositionResponse> items = page.getContent().stream().map(p -> {
            PositionResponse res = positionMapper.toResponse(p);
            res.setCompany(companySummary);
            return res;
        }).toList();

        return PageData.of(page, items);
    }
    @Transactional(readOnly = true)
    public Map<UUID, PositionSummary> getPositionSummaries(Set<UUID> positionIds) {
        if (positionIds == null || positionIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return positionRepository.findAllById(positionIds).stream()
                .collect(Collectors.toMap(
                        Position::getId,
                        p -> PositionSummary.builder()
                                .id(p.getId())
                                .code(p.getCode())
                                .name(p.getName())
                                .build()
                ));
    }
    @Transactional(readOnly = true)
    public PositionSummary getPositionSummary(UUID positionId) {
        if (positionId == null) {
            return null;
        }
        return positionRepository.findById(positionId)
                .map(p -> PositionSummary.builder()
                        .id(p.getId())
                        .code(p.getCode())
                        .name(p.getName())
                        .build())
                .orElse(null);
    }
    @Transactional(readOnly = true)
    public boolean existsPositionById(UUID positionId) {
        if (positionId == null) {
            return false;
        }
        return positionRepository.existsById(positionId);
    }
    @Transactional(readOnly = true)
    public Map<UUID, JobLevelSummary> getJobLevelSummaries(Set<UUID> jobLevelIds) {
        if (jobLevelIds == null || jobLevelIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return jobLevelRepository.findAllById(jobLevelIds).stream()
                .collect(Collectors.toMap(
                        JobLevel::getId,
                        jl -> JobLevelSummary.builder()
                                .id(jl.getId())
                                .name(jl.getName())
                                .rankOrder(jl.getRankOrder())
                                .build()
                ));
    }
    @Transactional(readOnly = true)
    public JobLevelSummary getJobLevelSummary(UUID jobLevelId) {
        if (jobLevelId == null) {
            return null;
        }
        return jobLevelRepository.findById(jobLevelId)
                .map(jl -> JobLevelSummary.builder()
                        .id(jl.getId())
                        .name(jl.getName())
                        .rankOrder(jl.getRankOrder())
                        .build())
                .orElse(null);
    }
    @Transactional(readOnly = true)
    public boolean existsJobLevelById(UUID jobLevelId) {
        if (jobLevelId == null) {
            return false;
        }
        return jobLevelRepository.existsById(jobLevelId);
    }
}
