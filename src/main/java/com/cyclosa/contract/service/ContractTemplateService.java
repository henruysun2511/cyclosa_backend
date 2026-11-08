package com.cyclosa.contract.service;

import com.cyclosa.common.exception.AppException;
import com.cyclosa.common.response.PageData;
import com.cyclosa.contract.dto.ContractTemplateDto;
import com.cyclosa.contract.entity.ContractTemplate;
import com.cyclosa.contract.entity.ContractTemplateVersion;
import com.cyclosa.contract.entity.enums.TemplateFormat;
import com.cyclosa.contract.entity.enums.TemplateKind;
import com.cyclosa.contract.exception.ContractErrorCode;
import com.cyclosa.contract.repository.ContractTemplateRepository;
import com.cyclosa.contract.repository.ContractTemplateVersionRepository;
import com.cyclosa.organization.repository.BranchRepository;
import com.cyclosa.organization.repository.CompanyRepository;
import com.cyclosa.organization.repository.DepartmentRepository;
import com.cyclosa.organization.repository.DepartmentVersionRepository;
import com.cyclosa.organization.repository.JobTitleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContractTemplateService {

    private final ContractTemplateRepository templateRepository;
    private final ContractTemplateVersionRepository versionRepository;
    private final CompanyRepository companyRepository;
    private final DepartmentRepository departmentRepository;
    private final DepartmentVersionRepository departmentVersionRepository;
    private final BranchRepository branchRepository;
    private final JobTitleRepository jobTitleRepository;

    @Transactional(readOnly = true)
    public PageData<ContractTemplateDto.TemplateResponse> getTemplates(
            UUID companyId,
            TemplateKind templateKind,
            Boolean active,
            Pageable pageable
    ) {
        Page<ContractTemplate> page = templateRepository.findByCompanyAndFilter(companyId, templateKind, active, pageable);
        return PageData.of(page, this::mapToTemplateResponse);
    }

    @Transactional(readOnly = true)
    public ContractTemplateDto.TemplateResponse getTemplateById(UUID id) {
        ContractTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new AppException(ContractErrorCode.CONTRACT_TEMPLATE_NOT_FOUND));
        return mapToTemplateResponse(template);
    }

    @Transactional
    public ContractTemplateDto.TemplateResponse createTemplate(ContractTemplateDto.CreateTemplateRequest request) {
        ContractTemplate template = ContractTemplate.builder()
                .companyId(request.getCompanyId())
                .name(request.getName().trim())
                .templateKind(request.getTemplateKind())
                .contractType(request.getContractType())
                .jobTitleId(request.getJobTitleId())
                .departmentId(request.getDepartmentId())
                .branchId(request.getBranchId())
                .active(true)
                .build();

        ContractTemplate saved = templateRepository.save(template);

        // Tạo phiên bản mẫu mặc định version 1
        ContractTemplateVersion defaultVersion = ContractTemplateVersion.builder()
                .template(saved)
                .versionNo(1)
                .format(TemplateFormat.DOCX)
                .fileUrl("https://storage.cyclosa.vn/templates/default-contract-v1.docx")
                .description("Phiên bản khởi tạo ban đầu")
                .build();
        versionRepository.save(defaultVersion);

        return mapToTemplateResponse(saved);
    }

    @Transactional
    public ContractTemplateDto.TemplateResponse updateTemplate(UUID id, ContractTemplateDto.UpdateTemplateRequest request) {
        ContractTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new AppException(ContractErrorCode.CONTRACT_TEMPLATE_NOT_FOUND));

        template.setName(request.getName().trim());
        template.setTemplateKind(request.getTemplateKind());
        template.setContractType(request.getContractType());
        template.setJobTitleId(request.getJobTitleId());
        template.setDepartmentId(request.getDepartmentId());
        template.setBranchId(request.getBranchId());
        if (request.getActive() != null) {
            template.setActive(request.getActive());
        }

        return mapToTemplateResponse(templateRepository.save(template));
    }

    @Transactional
    public void deactivateTemplate(UUID id) {
        ContractTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new AppException(ContractErrorCode.CONTRACT_TEMPLATE_NOT_FOUND));
        template.setActive(false);
        templateRepository.save(template);
    }

    @Transactional(readOnly = true)
    public List<ContractTemplateDto.TemplateVersionResponse> getVersions(UUID templateId) {
        return versionRepository.findByTemplateIdOrderByVersionNoDesc(templateId).stream()
                .map(this::mapToVersionResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ContractTemplateDto.TemplateVersionResponse createVersion(UUID templateId, ContractTemplateDto.CreateVersionRequest request) {
        ContractTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new AppException(ContractErrorCode.CONTRACT_TEMPLATE_NOT_FOUND));

        int nextVersionNo = versionRepository.findMaxVersionNoByTemplateId(templateId).orElse(0) + 1;

        ContractTemplateVersion version = ContractTemplateVersion.builder()
                .template(template)
                .versionNo(nextVersionNo)
                .format(request.getFormat())
                .fileUrl(request.getFileUrl())
                .description(request.getDescription())
                .build();

        return mapToVersionResponse(versionRepository.save(version));
    }

    public ContractTemplateDto.TemplateResponse mapToTemplateResponse(ContractTemplate t) {
        String jobTitleName = t.getJobTitleId() != null
                ? jobTitleRepository.findById(t.getJobTitleId()).map(j -> j.getName()).orElse(null)
                : null;
        String deptName = t.getDepartmentId() != null
                ? departmentVersionRepository.findLatestVersion(t.getDepartmentId()).map(v -> v.getName()).orElse(null)
                : null;
        String branchName = t.getBranchId() != null
                ? branchRepository.findById(t.getBranchId()).map(b -> b.getName()).orElse(null)
                : null;

        int vCount = t.getVersions() != null ? t.getVersions().size() : 0;

        return ContractTemplateDto.TemplateResponse.builder()
                .id(t.getId())
                .companyId(t.getCompanyId())
                .name(t.getName())
                .templateKind(t.getTemplateKind())
                .contractType(t.getContractType())
                .jobTitleId(t.getJobTitleId())
                .jobTitleName(jobTitleName)
                .departmentId(t.getDepartmentId())
                .departmentName(deptName)
                .branchId(t.getBranchId())
                .branchName(branchName)
                .active(t.isActive())
                .versionCount(vCount)
                .createdAt(t.getCreatedAt())
                .build();
    }

    public ContractTemplateDto.TemplateVersionResponse mapToVersionResponse(ContractTemplateVersion v) {
        return ContractTemplateDto.TemplateVersionResponse.builder()
                .id(v.getId())
                .templateId(v.getTemplate().getId())
                .versionNo(v.getVersionNo())
                .format(v.getFormat())
                .fileUrl(v.getFileUrl())
                .description(v.getDescription())
                .createdAt(v.getCreatedAt())
                .build();
    }
}
