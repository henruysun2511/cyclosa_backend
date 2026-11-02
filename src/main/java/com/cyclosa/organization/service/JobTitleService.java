package com.cyclosa.organization.service;

import com.cyclosa.common.exception.AppException;
import com.cyclosa.common.response.PageData;
import com.cyclosa.organization.dto.request.CreateJobTitleRequest;
import com.cyclosa.organization.dto.request.UpdateJobTitleRequest;
import com.cyclosa.organization.dto.response.JobTitleResponse;
import com.cyclosa.organization.entity.JobTitle;
import com.cyclosa.organization.exception.OrganizationErrorCode;
import com.cyclosa.organization.repository.CompanyRepository;
import com.cyclosa.organization.repository.JobTitleRepository;
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
public class JobTitleService {

    private final JobTitleRepository jobTitleRepository;
    private final CompanyRepository companyRepository;

    public PageData<JobTitleResponse> getJobTitles(UUID companyId, String keyword, Pageable pageable) {
        verifyCompanyExists(companyId);
        Page<JobTitle> page = jobTitleRepository.search(companyId, keyword, pageable);
        return PageData.of(page, this::mapToResponse);
    }

    public List<JobTitleResponse> getAllJobTitlesByCompany(UUID companyId) {
        verifyCompanyExists(companyId);
        return jobTitleRepository.findAllByCompanyId(companyId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public JobTitleResponse getJobTitleById(UUID id) {
        JobTitle jobTitle = findJobTitleOrThrow(id);
        return mapToResponse(jobTitle);
    }

    @Transactional
    public JobTitleResponse createJobTitle(CreateJobTitleRequest request) {
        verifyCompanyExists(request.getCompanyId());

        if (jobTitleRepository.existsByCompanyIdAndCode(request.getCompanyId(), request.getCode())) {
            throw new AppException(OrganizationErrorCode.JOB_TITLE_CODE_EXISTS);
        }

        JobTitle jobTitle = JobTitle.builder()
                .companyId(request.getCompanyId())
                .code(request.getCode().toUpperCase().trim())
                .name(request.getName().trim())
                .description(request.getDescription())
                .deleted(false)
                .build();

        jobTitle = jobTitleRepository.save(jobTitle);
        log.info("Đã tạo mới chức danh: id={}, code={}", jobTitle.getId(), jobTitle.getCode());
        return mapToResponse(jobTitle);
    }

    @Transactional
    public JobTitleResponse updateJobTitle(UUID id, UpdateJobTitleRequest request) {
        JobTitle jobTitle = findJobTitleOrThrow(id);
        jobTitle.setName(request.getName().trim());
        jobTitle.setDescription(request.getDescription());
        jobTitle = jobTitleRepository.save(jobTitle);
        log.info("Đã cập nhật chức danh: id={}", jobTitle.getId());
        return mapToResponse(jobTitle);
    }

    @Transactional
    public void deleteJobTitle(UUID id) {
        JobTitle jobTitle = findJobTitleOrThrow(id);
        jobTitleRepository.delete(jobTitle);
        log.info("Đã xóa mềm chức danh: id={}", id);
    }

    public JobTitle findJobTitleOrThrow(UUID id) {
        return jobTitleRepository.findById(id)
                .orElseThrow(() -> new AppException(OrganizationErrorCode.JOB_TITLE_NOT_FOUND));
    }

    private void verifyCompanyExists(UUID companyId) {
        if (!companyRepository.existsById(companyId)) {
            throw new AppException(OrganizationErrorCode.COMPANY_NOT_FOUND);
        }
    }

    private JobTitleResponse mapToResponse(JobTitle jobTitle) {
        return JobTitleResponse.builder()
                .id(jobTitle.getId())
                .companyId(jobTitle.getCompanyId())
                .code(jobTitle.getCode())
                .name(jobTitle.getName())
                .description(jobTitle.getDescription())
                .createdAt(jobTitle.getCreatedAt())
                .updatedAt(jobTitle.getUpdatedAt())
                .build();
    }
}

