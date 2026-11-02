package com.cyclosa.organization.service;

import com.cyclosa.common.exception.AppException;
import com.cyclosa.common.response.PageData;
import com.cyclosa.organization.dto.request.CreateJobLevelRequest;
import com.cyclosa.organization.dto.request.UpdateJobLevelRequest;
import com.cyclosa.organization.dto.response.JobLevelResponse;
import com.cyclosa.organization.entity.JobLevel;
import com.cyclosa.organization.exception.OrganizationErrorCode;
import com.cyclosa.organization.repository.CompanyRepository;
import com.cyclosa.organization.repository.JobLevelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JobLevelService {

    private final JobLevelRepository jobLevelRepository;
    private final CompanyRepository companyRepository;

    public PageData<JobLevelResponse> getJobLevels(UUID companyId, String keyword, Pageable pageable) {
        verifyCompanyExists(companyId);
        Page<JobLevel> page = jobLevelRepository.search(companyId, keyword, pageable);
        return PageData.of(page, this::mapToResponse);
    }

    public List<JobLevelResponse> getAllJobLevelsByCompany(UUID companyId) {
        verifyCompanyExists(companyId);
        return jobLevelRepository.findAllByCompanyIdOrderByRankOrderAsc(companyId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public JobLevelResponse getJobLevelById(UUID id) {
        JobLevel jobLevel = findJobLevelOrThrow(id);
        return mapToResponse(jobLevel);
    }

    @Transactional
    public JobLevelResponse createJobLevel(CreateJobLevelRequest request) {
        verifyCompanyExists(request.getCompanyId());

        if (jobLevelRepository.existsByCompanyIdAndCode(request.getCompanyId(), request.getCode())) {
            throw new AppException(OrganizationErrorCode.JOB_LEVEL_RANK_EXISTS);
        }

        if (jobLevelRepository.existsByCompanyIdAndRankOrder(request.getCompanyId(), request.getRankOrder())) {
            throw new AppException(OrganizationErrorCode.JOB_LEVEL_RANK_EXISTS);
        }

        JobLevel jobLevel = JobLevel.builder()
                .companyId(request.getCompanyId())
                .code(request.getCode().toUpperCase().trim())
                .name(request.getName().trim())
                .rankOrder(request.getRankOrder())
                .deleted(false)
                .build();

        jobLevel = jobLevelRepository.save(jobLevel);
        log.info("Đã tạo mới cấp bậc: id={}, code={}, rank={}", jobLevel.getId(), jobLevel.getCode(), jobLevel.getRankOrder());
        return mapToResponse(jobLevel);
    }

    @Transactional
    public JobLevelResponse updateJobLevel(UUID id, UpdateJobLevelRequest request) {
        JobLevel jobLevel = findJobLevelOrThrow(id);

        if (jobLevelRepository.existsByCompanyIdAndRankOrderAndIdNot(jobLevel.getCompanyId(), request.getRankOrder(), id)) {
            throw new AppException(OrganizationErrorCode.JOB_LEVEL_RANK_EXISTS);
        }

        jobLevel.setName(request.getName().trim());
        jobLevel.setRankOrder(request.getRankOrder());
        jobLevel = jobLevelRepository.save(jobLevel);
        log.info("Đã cập nhật cấp bậc: id={}, rank={}", jobLevel.getId(), jobLevel.getRankOrder());
        return mapToResponse(jobLevel);
    }

    @Transactional
    public void deleteJobLevel(UUID id) {
        JobLevel jobLevel = findJobLevelOrThrow(id);
        jobLevelRepository.delete(jobLevel);
        log.info("Đã xóa mềm cấp bậc: id={}", id);
    }

    public JobLevel findJobLevelOrThrow(UUID id) {
        return jobLevelRepository.findById(id)
                .orElseThrow(() -> new AppException(OrganizationErrorCode.JOB_LEVEL_NOT_FOUND));
    }

    private void verifyCompanyExists(UUID companyId) {
        if (!companyRepository.existsById(companyId)) {
            throw new AppException(OrganizationErrorCode.COMPANY_NOT_FOUND);
        }
    }

    private JobLevelResponse mapToResponse(JobLevel jobLevel) {
        return JobLevelResponse.builder()
                .id(jobLevel.getId())
                .companyId(jobLevel.getCompanyId())
                .code(jobLevel.getCode())
                .name(jobLevel.getName())
                .rankOrder(jobLevel.getRankOrder())
                .createdAt(jobLevel.getCreatedAt())
                .updatedAt(jobLevel.getUpdatedAt())
                .build();
    }
}

