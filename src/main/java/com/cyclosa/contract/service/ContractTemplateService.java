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

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
                .templateKind(request.getEffectiveTemplateKind())
                .contractType(request.getContractType())
                .jobTitleId(request.getJobTitleId())
                .departmentId(request.getDepartmentId())
                .branchId(request.getBranchId())
                .active(true)
                .build();

        ContractTemplate saved = templateRepository.save(template);

        // Tạo phiên bản mẫu mặc định version 1
        TemplateFormat format = request.getFormat() != null ? request.getFormat() : TemplateFormat.DOCX;
        String fileUrl = (request.getContentRaw() != null && !request.getContentRaw().isBlank())
                ? "https://storage.cyclosa.vn/templates/contract-v1.html"
                : "https://storage.cyclosa.vn/templates/default-contract-v1.docx";

        ContractTemplateVersion defaultVersion = ContractTemplateVersion.builder()
                .template(saved)
                .versionNo(1)
                .format(format)
                .fileUrl(fileUrl)
                .description(request.getDescription() != null ? request.getDescription() : "Phiên bản khởi tạo ban đầu")
                .build();
        versionRepository.save(defaultVersion);

        return mapToTemplateResponse(saved);
    }

    @Transactional
    public ContractTemplateDto.TemplateResponse updateTemplate(UUID id, ContractTemplateDto.UpdateTemplateRequest request) {
        ContractTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new AppException(ContractErrorCode.CONTRACT_TEMPLATE_NOT_FOUND));

        template.setName(request.getName().trim());
        template.setTemplateKind(request.getEffectiveTemplateKind());
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

        TemplateFormat format = request.getFormat() != null ? request.getFormat() : TemplateFormat.DOCX;
        String fileUrl = request.getFileUrl();
        if (fileUrl == null || fileUrl.isBlank()) {
            fileUrl = "https://storage.cyclosa.vn/templates/v" + nextVersionNo + (format == TemplateFormat.HTML ? ".html" : ".docx");
        }

        ContractTemplateVersion version = ContractTemplateVersion.builder()
                .template(template)
                .versionNo(nextVersionNo)
                .format(format)
                .fileUrl(fileUrl)
                .description(request.getDescription())
                .build();

        return mapToVersionResponse(versionRepository.save(version));
    }

    @Transactional
    public void publishVersion(UUID templateId, UUID versionId) {
        ContractTemplateVersion version = versionRepository.findById(versionId)
                .orElseThrow(() -> new AppException(ContractErrorCode.CONTRACT_TEMPLATE_NOT_FOUND));
        if (!version.getTemplate().getId().equals(templateId)) {
            throw new AppException(ContractErrorCode.CONTRACT_TEMPLATE_NOT_FOUND);
        }
    }

    public String renderPreview(ContractTemplateDto.RenderPreviewRequest request) {
        if (request == null) {
            return "<div style=\"padding: 20px; color: #888;\">Chưa có nội dung mẫu để xem trước.</div>";
        }

        String templateHtml = request.getContentRaw();
        if (templateHtml == null || templateHtml.isBlank()) {
            templateHtml = request.getContentPayload();
        }

        if ((templateHtml == null || templateHtml.isBlank()) && request.getVersionId() != null) {
            templateHtml = versionRepository.findById(request.getVersionId())
                    .map(ContractTemplateVersion::getFileUrl)
                    .orElse(null);
        }

        if (templateHtml == null || templateHtml.isBlank()) {
            return "<div style=\"padding: 20px; color: #888;\">Chưa có nội dung mẫu để xem trước. Vui lòng nhập khung HTML ở tab 'Nội dung Mẫu (HTML)'.</div>";
        }

        Map<String, Object> data = new HashMap<>();
        // Default preview values if not provided
        data.put("company_name", "TẬP ĐOÀN CÔNG NGHỆ CYCLOSA");
        data.put("employee_name", "NGUYỄN VĂN AN");
        data.put("employee_code", "EMP001");
        data.put("contract_number", "HDLD-2026-001");
        data.put("start_date", "01/01/2026");
        data.put("base_salary", "20,000,000 VND");
        data.put("job_title", "Kỹ sư phần mềm cao cấp");
        data.put("work_location", "Tòa nhà CYCLOSA Tower, Quận 1, TP.HCM");

        if (request.getData() != null) {
            data.putAll(request.getData());
        }

        if (request.getSampleDataJson() != null && !request.getSampleDataJson().isBlank()) {
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                Map<String, Object> parsed = mapper.readValue(
                        request.getSampleDataJson(),
                        new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {}
                );
                if (parsed != null) {
                    data.putAll(parsed);
                }
            } catch (Exception ignored) {
            }
        }

        return renderTemplateWithData(templateHtml, data);
    }

    private String renderTemplateWithData(String template, Map<String, Object> data) {
        if (template == null) return "";
        String result = template;

        // 1. Process #if conditionals: {{#if key}}(.*?)(?:\{\{else\}\}(.*?))?\{\{/if\}\}
        Pattern ifPattern = Pattern.compile("\\{\\{#if\\s+([a-zA-Z0-9_.-]+)\\}\\}(.*?)(?:\\{\\{else\\}\\}(.*?))?\\{\\{/if\\}\\}", Pattern.DOTALL);
        Matcher ifMatcher = ifPattern.matcher(result);
        StringBuffer sbIf = new StringBuffer();
        while (ifMatcher.find()) {
            String key = ifMatcher.group(1);
            String trueBranch = ifMatcher.group(2);
            String falseBranch = ifMatcher.group(3) != null ? ifMatcher.group(3) : "";
            Object val = getNestedValue(data, key);
            boolean isTruthy = isTruthy(val);
            String chosen = isTruthy ? trueBranch : falseBranch;
            ifMatcher.appendReplacement(sbIf, Matcher.quoteReplacement(chosen));
        }
        ifMatcher.appendTail(sbIf);
        result = sbIf.toString();

        // 2. Process #unless: {{#unless key}}(.*?)\{\{/unless\}\}
        Pattern unlessPattern = Pattern.compile("\\{\\{#unless\\s+([a-zA-Z0-9_.-]+)\\}\\}(.*?)\\{\\{/unless\\}\\}", Pattern.DOTALL);
        Matcher unlessMatcher = unlessPattern.matcher(result);
        StringBuffer sbUnless = new StringBuffer();
        while (unlessMatcher.find()) {
            String key = unlessMatcher.group(1);
            String body = unlessMatcher.group(2);
            Object val = getNestedValue(data, key);
            boolean isTruthy = isTruthy(val);
            String chosen = !isTruthy ? body : "";
            unlessMatcher.appendReplacement(sbUnless, Matcher.quoteReplacement(chosen));
        }
        unlessMatcher.appendTail(sbUnless);
        result = sbUnless.toString();

        // 3. Process variables: {{{key}}} and {{key}}
        Pattern varPattern = Pattern.compile("\\{\\{\\{?\\s*([a-zA-Z0-9_.-]+)\\s*\\}\\}\\}?");
        Matcher varMatcher = varPattern.matcher(result);
        StringBuffer sbVar = new StringBuffer();
        while (varMatcher.find()) {
            String key = varMatcher.group(1);
            Object val = getNestedValue(data, key);
            String rep = val != null ? String.valueOf(val) : "";
            varMatcher.appendReplacement(sbVar, Matcher.quoteReplacement(rep));
        }
        varMatcher.appendTail(sbVar);
        return sbVar.toString();
    }

    private Object getNestedValue(Map<String, Object> data, String key) {
        if (data == null || key == null) return null;
        if (data.containsKey(key)) {
            return data.get(key);
        }
        // Case-insensitive fallback
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(key)) {
                return entry.getValue();
            }
        }
        // Handle dot notation "employee.name"
        if (key.contains(".")) {
            String[] parts = key.split("\\.", 2);
            Object parent = data.get(parts[0]);
            if (parent instanceof Map) {
                return getNestedValue((Map<String, Object>) parent, parts[1]);
            }
        }
        return null;
    }

    private boolean isTruthy(Object val) {
        if (val == null) return false;
        if (val instanceof Boolean b) return b;
        if (val instanceof String s) return !s.isBlank() && !s.equalsIgnoreCase("false") && !s.equals("0");
        if (val instanceof Number n) return n.doubleValue() != 0.0;
        if (val instanceof Collection<?> c) return !c.isEmpty();
        return true;
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
