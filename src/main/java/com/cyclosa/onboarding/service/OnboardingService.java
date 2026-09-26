package com.cyclosa.onboarding.service;

import com.cyclosa.asset.dto.response.AssetAllocationResponse;
import com.cyclosa.asset.service.AssetService;
import com.cyclosa.common.dto.summary.EmployeeSummary;
import com.cyclosa.common.dto.summary.OrgUnitSummary;
import com.cyclosa.common.dto.summary.PositionSummary;
import com.cyclosa.common.exception.AppException;
import com.cyclosa.common.response.PageData;
import com.cyclosa.employee.dto.response.EmployeeDetailResponse;
import com.cyclosa.employee.enums.EmploymentStatus;
import com.cyclosa.employee.exception.EmployeeErrorCode;
import com.cyclosa.employee.service.EmployeeService;
import com.cyclosa.onboarding.dto.filter.ChecklistTemplateFilter;
import com.cyclosa.onboarding.dto.filter.OnboardingProcessFilter;
import com.cyclosa.onboarding.dto.request.*;
import com.cyclosa.onboarding.dto.response.*;
import com.cyclosa.onboarding.entity.*;
import com.cyclosa.onboarding.enums.OnboardingItemCategory;
import com.cyclosa.onboarding.enums.OnboardingItemStatus;
import com.cyclosa.onboarding.enums.OnboardingStatus;
import com.cyclosa.onboarding.enums.ProvisioningStatus;
import com.cyclosa.onboarding.exception.OnboardingErrorCode;
import com.cyclosa.onboarding.mapper.OnboardingMapper;
import com.cyclosa.onboarding.repository.*;
import com.cyclosa.organization.service.OrganizationalUnitService;
import com.cyclosa.organization.service.PositionService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OnboardingService {

    private static final Set<String> ALLOWED_TEMPLATE_SORT_FIELDS = Set.of("createdAt", "updatedAt", "name", "orderIndex");
    private static final Set<String> ALLOWED_PROCESS_SORT_FIELDS = Set.of("createdAt", "updatedAt", "startDate", "status");

    private final OnboardingChecklistTemplateRepository templateRepository;
    private final OnboardingChecklistTemplateItemRepository templateItemRepository;
    private final OnboardingProcessRepository processRepository;
    private final OnboardingProcessItemRepository processItemRepository;
    private final AccountProvisioningRepository accountRepository;
    private final OrientationSessionRepository sessionRepository;
    private final EmployeeDocumentRepository documentRepository;

    // Public services from other modules (Strict Modular Monolith: NO cross-module repository injection!)
    private final EmployeeService employeeService;
    private final OrganizationalUnitService orgUnitService;
    private final PositionService positionService;
    private final AssetService assetService;

    private final OnboardingMapper onboardingMapper;

    // =========================================================================
    // 1. CHECKLIST TEMPLATES
    // =========================================================================

    @Transactional
    public ChecklistTemplateDetailResponse createTemplate(UUID companyId, CreateChecklistTemplateRequest request) {
        UUID effectiveCompanyId = request.getCompanyId() != null ? request.getCompanyId() : companyId;
        OnboardingChecklistTemplate template = onboardingMapper.toEntity(request);
        template.setCompanyId(effectiveCompanyId);
        template.setIsActive(true);

        if (request.getItems() != null && !request.getItems().isEmpty()) {
            List<OnboardingChecklistTemplateItem> items = new ArrayList<>();
            for (int i = 0; i < request.getItems().size(); i++) {
                CreateTemplateItemRequest itemReq = request.getItems().get(i);
                OnboardingChecklistTemplateItem item = onboardingMapper.toEntity(itemReq);
                item.setTemplate(template);
                if (item.getOrderIndex() == null || item.getOrderIndex() == 0) {
                    item.setOrderIndex(i + 1);
                }
                items.add(item);
            }
            template.setItems(items);
        }

        OnboardingChecklistTemplate saved = templateRepository.save(template);
        log.info("Created OnboardingChecklistTemplate id={}, name={}", saved.getId(), saved.getName());
        return toTemplateDetailResponse(saved);
    }

    @Transactional(readOnly = true)
    public PageData<ChecklistTemplateResponse> getTemplates(UUID companyId, ChecklistTemplateFilter filter) {
        Specification<OnboardingChecklistTemplate> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (companyId != null) {
                predicates.add(cb.or(cb.equal(root.get("companyId"), companyId), cb.isNull(root.get("companyId"))));
            }
            if (filter != null) {
                if (filter.getApplicablePositionId() != null) {
                    predicates.add(cb.equal(root.get("applicablePositionId"), filter.getApplicablePositionId()));
                }
                if (filter.getApplicableDepartmentId() != null) {
                    predicates.add(cb.equal(root.get("applicableDepartmentId"), filter.getApplicableDepartmentId()));
                }
                if (filter.getIsActive() != null) {
                    predicates.add(cb.equal(root.get("isActive"), filter.getIsActive()));
                }
                if (filter.getKeyword() != null && !filter.getKeyword().trim().isEmpty()) {
                    String kw = "%" + filter.getKeyword().trim().toLowerCase() + "%";
                    predicates.add(cb.or(
                            cb.like(cb.lower(root.get("name")), kw),
                            cb.like(cb.lower(root.get("description")), kw)
                    ));
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        org.springframework.data.domain.Pageable effectivePageable = (filter != null)
                ? filter.toPageable("createdAt", ALLOWED_TEMPLATE_SORT_FIELDS)
                : org.springframework.data.domain.PageRequest.of(0, 20, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt"));

        Page<OnboardingChecklistTemplate> page = templateRepository.findAll(spec, effectivePageable);
        if (page.isEmpty()) {
            return PageData.of(page, List.of());
        }

        // Batch enrichment for Nested Summary Objects (Rule 8.3)
        Set<UUID> posIds = page.getContent().stream()
                .map(OnboardingChecklistTemplate::getApplicablePositionId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<UUID, PositionSummary> posMap = positionService.getPositionSummaries(posIds);

        Set<UUID> deptIds = page.getContent().stream()
                .map(OnboardingChecklistTemplate::getApplicableDepartmentId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<UUID, OrgUnitSummary> deptMap = orgUnitService.getUnitSummaries(deptIds);

        List<ChecklistTemplateResponse> content = page.getContent().stream()
                .map(t -> {
                    ChecklistTemplateResponse res = onboardingMapper.toResponse(t);
                    if (t.getApplicablePositionId() != null) {
                        PositionSummary ps = posMap.get(t.getApplicablePositionId());
                        if (ps != null) {
                            res.setApplicablePosition(ps);
                            res.setApplicablePositionTitle(ps.getName());
                        }
                    }
                    if (t.getApplicableDepartmentId() != null) {
                        OrgUnitSummary ds = deptMap.get(t.getApplicableDepartmentId());
                        if (ds != null) {
                            res.setApplicableDepartment(ds);
                            res.setApplicableDepartmentName(ds.getName());
                        }
                    }
                    res.setTotalItems(t.getItems() != null ? t.getItems().size() : 0);
                    return res;
                })
                .toList();

        return PageData.of(page, content);
    }

    @Transactional(readOnly = true)
    public ChecklistTemplateDetailResponse getTemplateById(UUID companyId, UUID id) {
        OnboardingChecklistTemplate template = findTemplate(companyId, id);
        return toTemplateDetailResponse(template);
    }

    @Transactional
    public ChecklistTemplateDetailResponse updateTemplate(UUID companyId, UUID id, UpdateChecklistTemplateRequest request) {
        OnboardingChecklistTemplate template = findTemplate(companyId, id);
        if (request.getName() != null) template.setName(request.getName());
        if (request.getApplicablePositionId() != null) template.setApplicablePositionId(request.getApplicablePositionId());
        if (request.getApplicableDepartmentId() != null) template.setApplicableDepartmentId(request.getApplicableDepartmentId());
        if (request.getDescription() != null) template.setDescription(request.getDescription());
        if (request.getIsActive() != null) template.setIsActive(request.getIsActive());

        OnboardingChecklistTemplate saved = templateRepository.save(template);
        return toTemplateDetailResponse(saved);
    }

    @Transactional
    public void deleteTemplate(UUID companyId, UUID id) {
        OnboardingChecklistTemplate template = findTemplate(companyId, id);
        templateRepository.delete(template);
        log.info("Deleted OnboardingChecklistTemplate id={}", id);
    }

    @Transactional
    public ChecklistTemplateItemResponse addItemToTemplate(UUID companyId, UUID templateId, CreateTemplateItemRequest request) {
        OnboardingChecklistTemplate template = findTemplate(companyId, templateId);
        OnboardingChecklistTemplateItem item = onboardingMapper.toEntity(request);
        item.setTemplate(template);
        if (item.getOrderIndex() == null || item.getOrderIndex() == 0) {
            item.setOrderIndex(template.getItems().size() + 1);
        }
        OnboardingChecklistTemplateItem saved = templateItemRepository.save(item);
        return onboardingMapper.toItemResponse(saved);
    }

    @Transactional
    public ChecklistTemplateItemResponse updateTemplateItem(UUID companyId, UUID itemId, UpdateTemplateItemRequest request) {
        OnboardingChecklistTemplateItem item = templateItemRepository.findById(itemId)
                .orElseThrow(() -> new AppException(OnboardingErrorCode.TEMPLATE_ITEM_NOT_FOUND));

        if (companyId != null && item.getTemplate().getCompanyId() != null && !item.getTemplate().getCompanyId().equals(companyId)) {
            throw new AppException(OnboardingErrorCode.TEMPLATE_ITEM_NOT_FOUND);
        }

        if (request.getTitle() != null) item.setTitle(request.getTitle());
        if (request.getDescription() != null) item.setDescription(request.getDescription());
        if (request.getCategory() != null) item.setCategory(request.getCategory());
        if (request.getOrderIndex() != null) item.setOrderIndex(request.getOrderIndex());
        if (request.getIsRequired() != null) item.setIsRequired(request.getIsRequired());

        OnboardingChecklistTemplateItem saved = templateItemRepository.save(item);
        return onboardingMapper.toItemResponse(saved);
    }

    @Transactional
    public void deleteTemplateItem(UUID companyId, UUID itemId) {
        OnboardingChecklistTemplateItem item = templateItemRepository.findById(itemId)
                .orElseThrow(() -> new AppException(OnboardingErrorCode.TEMPLATE_ITEM_NOT_FOUND));

        if (companyId != null && item.getTemplate().getCompanyId() != null && !item.getTemplate().getCompanyId().equals(companyId)) {
            throw new AppException(OnboardingErrorCode.TEMPLATE_ITEM_NOT_FOUND);
        }

        templateItemRepository.delete(item);
    }

    // =========================================================================
    // 2. ONBOARDING PROCESSES
    // =========================================================================

    @Transactional
    public OnboardingProcessDetailResponse createProcess(UUID companyId, CreateOnboardingProcessRequest request) {
        // 1. Validate employee through EmployeeService
        EmployeeDetailResponse employee = employeeService.getEmployeeByIdInternal(request.getEmployeeId());
        if (employee == null) {
            throw new AppException(EmployeeErrorCode.EMPLOYEE_NOT_FOUND);
        }

        UUID effectiveCompanyId = request.getCompanyId() != null ? request.getCompanyId() : companyId;
        if (effectiveCompanyId == null && employee.getCompany() != null) {
            effectiveCompanyId = employee.getCompany().getId();
        }

        // 2. Check no active onboarding in progress
        boolean hasInProgress = processRepository.existsByEmployeeIdAndStatus(employee.getId(), OnboardingStatus.IN_PROGRESS);
        if (hasInProgress) {
            throw new AppException(OnboardingErrorCode.ONBOARDING_ALREADY_IN_PROGRESS);
        }

        // 3. Validate checklist template
        OnboardingChecklistTemplate template = templateRepository.findById(request.getChecklistTemplateId())
                .orElseThrow(() -> new AppException(OnboardingErrorCode.CHECKLIST_TEMPLATE_NOT_FOUND));

        // 4. Create process
        OnboardingProcess process = OnboardingProcess.builder()
                .companyId(effectiveCompanyId)
                .employeeId(employee.getId())
                .checklistTemplateId(template.getId())
                .startDate(request.getStartDate() != null ? request.getStartDate() : LocalDate.now())
                .status(OnboardingStatus.IN_PROGRESS)
                .notes(request.getNotes())
                .build();

        // 5. Clone items from template
        List<OnboardingChecklistTemplateItem> templateItems = templateItemRepository.findByTemplateIdOrderByOrderIndexAsc(template.getId());
        List<OnboardingProcessItem> processItems = new ArrayList<>();
        for (OnboardingChecklistTemplateItem ti : templateItems) {
            OnboardingProcessItem pi = OnboardingProcessItem.builder()
                    .onboardingProcess(process)
                    .templateItemId(ti.getId())
                    .title(ti.getTitle())
                    .description(ti.getDescription())
                    .category(ti.getCategory())
                    .orderIndex(ti.getOrderIndex())
                    .isRequired(ti.getIsRequired())
                    .status(OnboardingItemStatus.PENDING)
                    .build();
            processItems.add(pi);
        }
        process.setItems(processItems);

        OnboardingProcess saved = processRepository.save(process);
        log.info("Started OnboardingProcess id={} for employeeId={}", saved.getId(), employee.getId());

        return toProcessDetailResponse(saved);
    }

    @Transactional(readOnly = true)
    public PageData<OnboardingProcessResponse> getProcesses(UUID companyId, OnboardingProcessFilter filter) {
        Specification<OnboardingProcess> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (companyId != null) predicates.add(cb.equal(root.get("companyId"), companyId));
            if (filter != null) {
                if (filter.getEmployeeId() != null) predicates.add(cb.equal(root.get("employeeId"), filter.getEmployeeId()));
                if (filter.getChecklistTemplateId() != null) predicates.add(cb.equal(root.get("checklistTemplateId"), filter.getChecklistTemplateId()));
                if (filter.getStatus() != null) predicates.add(cb.equal(root.get("status"), filter.getStatus()));
                if (filter.getStartDateFrom() != null) predicates.add(cb.greaterThanOrEqualTo(root.get("startDate"), filter.getStartDateFrom()));
                if (filter.getStartDateTo() != null) predicates.add(cb.lessThanOrEqualTo(root.get("startDate"), filter.getStartDateTo()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        org.springframework.data.domain.Pageable effectivePageable = (filter != null)
                ? filter.toPageable("createdAt", ALLOWED_PROCESS_SORT_FIELDS)
                : org.springframework.data.domain.PageRequest.of(0, 20, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt"));

        Page<OnboardingProcess> page = processRepository.findAll(spec, effectivePageable);
        if (page.isEmpty()) {
            return PageData.of(page, List.of());
        }

        // Batch enrichment for Employee Summaries (Rule 8.3)
        Set<UUID> empIds = page.getContent().stream()
                .map(OnboardingProcess::getEmployeeId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<UUID, EmployeeSummary> empMap = employeeService.getEmployeeSummaries(empIds);

        Set<UUID> tmplIds = page.getContent().stream()
                .map(OnboardingProcess::getChecklistTemplateId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<UUID, String> tmplNameMap = templateRepository.findAllById(tmplIds).stream()
                .collect(Collectors.toMap(OnboardingChecklistTemplate::getId, OnboardingChecklistTemplate::getName, (a, b) -> a));

        // Batch count for Process Items (chống N+1 Query - Rule 8.3)
        Set<UUID> processIds = page.getContent().stream()
                .map(OnboardingProcess::getId)
                .collect(Collectors.toSet());
        Map<UUID, long[]> countsMap = new HashMap<>();
        List<Object[]> rawCounts = processItemRepository.countItemsByProcessIds(processIds);
        for (Object[] row : rawCounts) {
            UUID pId = (UUID) row[0];
            long total = ((Number) row[1]).longValue();
            long completed = row[2] != null ? ((Number) row[2]).longValue() : 0L;
            countsMap.put(pId, new long[]{total, completed});
        }

        List<OnboardingProcessResponse> content = page.getContent().stream()
                .map(process -> {
                    OnboardingProcessResponse res = onboardingMapper.toProcessResponse(process);
                    EmployeeSummary emp = empMap.get(process.getEmployeeId());
                    if (emp != null) {
                        res.setEmployee(emp);
                        res.setEmployeeName(emp.getFullName());
                        res.setEmployeeCode(emp.getEmployeeCode());
                    }
                    res.setChecklistTemplateName(tmplNameMap.get(process.getChecklistTemplateId()));

                    long[] counts = countsMap.getOrDefault(process.getId(), new long[]{0L, 0L});
                    long total = counts[0];
                    long completed = counts[1];
                    res.setTotalItems(total);
                    res.setCompletedItems(completed);
                    res.setProgressPercentage(total > 0 ? Math.round((double) completed / total * 100.0 * 10.0) / 10.0 : 0.0);
                    return res;
                })
                .toList();

        return PageData.of(page, content);
    }

    @Transactional(readOnly = true)
    public OnboardingProcessDetailResponse getProcessById(UUID companyId, UUID id) {
        OnboardingProcess process = findProcess(companyId, id);
        return toProcessDetailResponse(process);
    }

    @Transactional(readOnly = true)
    public OnboardingProcessDetailResponse getProcessByEmployeeId(UUID companyId, UUID employeeId) {
        List<OnboardingProcess> list = processRepository.findByEmployeeIdOrderByCreatedAtDesc(employeeId);
        if (list.isEmpty()) {
            throw new AppException(OnboardingErrorCode.ONBOARDING_PROCESS_NOT_FOUND);
        }
        OnboardingProcess process = list.get(0);
        if (companyId != null && !process.getCompanyId().equals(companyId)) {
            throw new AppException(OnboardingErrorCode.ONBOARDING_PROCESS_NOT_FOUND);
        }
        return toProcessDetailResponse(process);
    }

    @Transactional(readOnly = true)
    public OnboardingProcessDetailResponse getMyProcess(UUID userId) {
        UUID employeeId = employeeService.findEmployeeIdByUserId(userId)
                .orElseThrow(() -> new AppException(EmployeeErrorCode.EMPLOYEE_NOT_FOUND));

        List<OnboardingProcess> list = processRepository.findByEmployeeIdOrderByCreatedAtDesc(employeeId);
        if (list.isEmpty()) {
            throw new AppException(OnboardingErrorCode.ONBOARDING_PROCESS_NOT_FOUND);
        }
        return toProcessDetailResponse(list.get(0));
    }

    @Transactional(readOnly = true)
    public OnboardingProgressResponse getProgress(UUID companyId, UUID processId) {
        OnboardingProcess process = findProcess(companyId, processId);
        return calculateProgress(process);
    }

    @Transactional
    public OnboardingProcessItemResponse completeProcessItem(UUID companyId, UUID itemId, CompleteProcessItemRequest request, UUID currentUserId) {
        OnboardingProcessItem item = processItemRepository.findById(itemId)
                .orElseThrow(() -> new AppException(OnboardingErrorCode.PROCESS_ITEM_NOT_FOUND));

        OnboardingProcess process = item.getOnboardingProcess();
        if (companyId != null && !process.getCompanyId().equals(companyId)) {
            throw new AppException(OnboardingErrorCode.PROCESS_ITEM_NOT_FOUND);
        }

        if (process.getStatus() != OnboardingStatus.IN_PROGRESS) {
            throw new AppException(OnboardingErrorCode.ONBOARDING_PROCESS_CLOSED);
        }

        if (item.getStatus() == OnboardingItemStatus.COMPLETED) {
            throw new AppException(OnboardingErrorCode.ITEM_ALREADY_COMPLETED);
        }

        // Validate BA rule: If category == ACCOUNT, must have at least one account_provisioning in PROVISIONED status
        if (item.getCategory() == OnboardingItemCategory.ACCOUNT) {
            boolean hasProvisioned = accountRepository.existsByOnboardingProcessIdAndStatus(process.getId(), ProvisioningStatus.PROVISIONED)
                    || accountRepository.existsByEmployeeIdAndStatus(process.getEmployeeId(), ProvisioningStatus.PROVISIONED);
            if (!hasProvisioned) {
                throw new AppException(OnboardingErrorCode.ACCOUNT_NOT_PROVISIONED_YET);
            }
        }

        UUID completedBy = (request != null && request.getCompletedByEmployeeId() != null)
                ? request.getCompletedByEmployeeId()
                : resolveCurrentEmployeeId(currentUserId);

        item.setStatus(OnboardingItemStatus.COMPLETED);
        item.setCompletedAt(LocalDateTime.now());
        item.setCompletedByEmployeeId(completedBy);
        if (request != null && request.getNote() != null) {
            item.setNote(request.getNote());
        }

        OnboardingProcessItem saved = processItemRepository.save(item);
        log.info("Completed OnboardingProcessItem id={}, processId={}", saved.getId(), process.getId());

        OnboardingProcessItemResponse res = onboardingMapper.toProcessItemResponse(saved);
        if (completedBy != null) {
            EmployeeSummary empSummary = employeeService.getEmployeeSummary(completedBy);
            if (empSummary != null) {
                res.setCompletedByEmployeeName(empSummary.getFullName());
            }
        }
        return res;
    }

    @Transactional
    public OnboardingProcessItemResponse skipProcessItem(UUID companyId, UUID itemId, String note, UUID currentUserId) {
        OnboardingProcessItem item = processItemRepository.findById(itemId)
                .orElseThrow(() -> new AppException(OnboardingErrorCode.PROCESS_ITEM_NOT_FOUND));

        OnboardingProcess process = item.getOnboardingProcess();
        if (companyId != null && !process.getCompanyId().equals(companyId)) {
            throw new AppException(OnboardingErrorCode.PROCESS_ITEM_NOT_FOUND);
        }

        if (process.getStatus() != OnboardingStatus.IN_PROGRESS) {
            throw new AppException(OnboardingErrorCode.ONBOARDING_PROCESS_CLOSED);
        }

        UUID currentEmpId = resolveCurrentEmployeeId(currentUserId);
        item.setStatus(OnboardingItemStatus.SKIPPED);
        item.setCompletedAt(LocalDateTime.now());
        item.setCompletedByEmployeeId(currentEmpId);
        item.setNote(note);

        OnboardingProcessItem saved = processItemRepository.save(item);
        return onboardingMapper.toProcessItemResponse(saved);
    }

    @Transactional
    public OnboardingProcessDetailResponse completeProcess(UUID companyId, UUID processId, UUID currentUserId) {
        OnboardingProcess process = findProcess(companyId, processId);

        if (process.getStatus() != OnboardingStatus.IN_PROGRESS) {
            throw new AppException(OnboardingErrorCode.ONBOARDING_PROCESS_CLOSED);
        }

        // Validate all required items are completed
        long incompleteRequired = processItemRepository.countIncompleteRequiredItems(processId);
        if (incompleteRequired > 0) {
            throw new AppException(OnboardingErrorCode.REQUIRED_ITEMS_NOT_COMPLETED);
        }

        process.setStatus(OnboardingStatus.COMPLETED);
        process.setCompletedAt(LocalDateTime.now());
        OnboardingProcess saved = processRepository.save(process);

        // BA Rule: 100% checklist xong -> chuyển Employee status sang ACTIVE qua Public EmployeeService
        try {
            employeeService.updateEmploymentStatus(process.getEmployeeId(), EmploymentStatus.ACTIVE);
            log.info("Activated Employee id={} to ACTIVE upon Onboarding completion", process.getEmployeeId());
        } catch (Exception e) {
            log.warn("Could not update employment status via EmployeeService: {}", e.getMessage());
        }

        log.info("Successfully completed OnboardingProcess id={}", saved.getId());
        return toProcessDetailResponse(saved);
    }

    @Transactional
    public OnboardingProcessDetailResponse cancelProcess(UUID companyId, UUID processId, String reason) {
        OnboardingProcess process = findProcess(companyId, processId);
        if (process.getStatus() == OnboardingStatus.COMPLETED) {
            throw new AppException(OnboardingErrorCode.ONBOARDING_CANNOT_CANCEL);
        }

        process.setStatus(OnboardingStatus.CANCELLED);
        if (reason != null && !reason.trim().isEmpty()) {
            process.setNotes((process.getNotes() != null ? process.getNotes() + "\n" : "") + "Lý do hủy: " + reason);
        }
        OnboardingProcess saved = processRepository.save(process);
        return toProcessDetailResponse(saved);
    }

    // =========================================================================
    // 3. ACCOUNT PROVISIONING
    // =========================================================================

    @Transactional
    public AccountProvisioningResponse createAccountProvisioning(UUID companyId, UUID processId, CreateAccountProvisioningRequest request, UUID currentUserId) {
        OnboardingProcess process = findProcess(companyId, processId);
        UUID empId = request.getEmployeeId() != null ? request.getEmployeeId() : process.getEmployeeId();

        AccountProvisioning account = AccountProvisioning.builder()
                .companyId(process.getCompanyId())
                .onboardingProcessId(process.getId())
                .employeeId(empId)
                .systemName(request.getSystemName())
                .accountUsername(request.getAccountUsername())
                .status(ProvisioningStatus.PENDING)
                .notes(request.getNotes())
                .build();

        AccountProvisioning saved = accountRepository.save(account);
        return toAccountResponse(saved);
    }

    @Transactional
    public AccountProvisioningResponse updateAccountProvisioningStatus(UUID companyId, UUID accountId, UpdateAccountProvisioningRequest request, UUID currentUserId) {
        AccountProvisioning account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AppException(OnboardingErrorCode.ACCOUNT_PROVISIONING_NOT_FOUND));

        if (companyId != null && !account.getCompanyId().equals(companyId)) {
            throw new AppException(OnboardingErrorCode.ACCOUNT_PROVISIONING_NOT_FOUND);
        }

        account.setStatus(request.getStatus());
        UUID provisionedBy = request.getProvisionedByEmployeeId() != null
                ? request.getProvisionedByEmployeeId()
                : resolveCurrentEmployeeId(currentUserId);
        account.setProvisionedByEmployeeId(provisionedBy);

        if (request.getStatus() == ProvisioningStatus.PROVISIONED) {
            account.setProvisionedAt(LocalDateTime.now());
        } else if (request.getStatus() == ProvisioningStatus.REVOKED) {
            account.setRevokedAt(LocalDateTime.now());
        }

        if (request.getNotes() != null) {
            account.setNotes(request.getNotes());
        }

        AccountProvisioning saved = accountRepository.save(account);
        return toAccountResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<AccountProvisioningResponse> getAccountsByProcessId(UUID companyId, UUID processId) {
        OnboardingProcess process = findProcess(companyId, processId);
        List<AccountProvisioning> list = accountRepository.findByOnboardingProcessId(process.getId());
        return list.stream().map(this::toAccountResponse).toList();
    }

    // =========================================================================
    // 4. ORIENTATION SESSIONS
    // =========================================================================

    @Transactional
    public OrientationSessionResponse createOrientationSession(UUID companyId, UUID processId, CreateOrientationSessionRequest request) {
        OnboardingProcess process = findProcess(companyId, processId);

        OrientationSession session = OrientationSession.builder()
                .companyId(process.getCompanyId())
                .onboardingProcessId(process.getId())
                .sessionName(request.getSessionName())
                .description(request.getDescription())
                .location(request.getLocation())
                .scheduledAt(request.getScheduledAt())
                .trainerEmployeeId(request.getTrainerEmployeeId())
                .build();

        OrientationSession saved = sessionRepository.save(session);
        return toSessionResponse(saved);
    }

    @Transactional
    public OrientationSessionResponse updateOrientationSession(UUID companyId, UUID sessionId, UpdateOrientationSessionRequest request) {
        OrientationSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new AppException(OnboardingErrorCode.ORIENTATION_SESSION_NOT_FOUND));

        if (companyId != null && !session.getCompanyId().equals(companyId)) {
            throw new AppException(OnboardingErrorCode.ORIENTATION_SESSION_NOT_FOUND);
        }

        if (request.getSessionName() != null) session.setSessionName(request.getSessionName());
        if (request.getDescription() != null) session.setDescription(request.getDescription());
        if (request.getLocation() != null) session.setLocation(request.getLocation());
        if (request.getScheduledAt() != null) session.setScheduledAt(request.getScheduledAt());
        if (request.getTrainerEmployeeId() != null) session.setTrainerEmployeeId(request.getTrainerEmployeeId());
        if (request.getStatus() != null) session.setStatus(request.getStatus());

        OrientationSession saved = sessionRepository.save(session);
        return toSessionResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<OrientationSessionResponse> getOrientationSessionsByProcessId(UUID companyId, UUID processId) {
        OnboardingProcess process = findProcess(companyId, processId);
        List<OrientationSession> list = sessionRepository.findByOnboardingProcessIdOrderByScheduledAtAsc(process.getId());
        return list.stream().map(this::toSessionResponse).toList();
    }

    // =========================================================================
    // 5. EMPLOYEE DOCUMENTS
    // =========================================================================

    @Transactional
    public EmployeeDocumentResponse uploadDocument(UUID companyId, UUID employeeId, UploadEmployeeDocumentRequest request, UUID currentUserId) {
        EmployeeDetailResponse empDetail = employeeService.getEmployeeByIdInternal(employeeId);
        if (empDetail == null) {
            throw new AppException(EmployeeErrorCode.EMPLOYEE_NOT_FOUND);
        }

        UUID compId = empDetail.getCompany() != null ? empDetail.getCompany().getId() : companyId;
        EmployeeDocument document = EmployeeDocument.builder()
                .companyId(compId)
                .employeeId(empDetail.getId())
                .documentType(request.getDocumentType())
                .documentName(request.getDocumentName())
                .fileUrl(request.getFileUrl())
                .fileSize(request.getFileSize())
                .uploadedAt(LocalDateTime.now())
                .isVerified(false)
                .notes(request.getNotes())
                .build();

        EmployeeDocument saved = documentRepository.save(document);
        log.info("Uploaded EmployeeDocument id={} for employeeId={}", saved.getId(), empDetail.getId());
        return toDocumentResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<EmployeeDocumentResponse> getDocumentsByEmployeeId(UUID companyId, UUID employeeId) {
        employeeService.getEmployeeByIdInternal(employeeId);
        List<EmployeeDocument> list = documentRepository.findByEmployeeIdOrderByUploadedAtDesc(employeeId);
        return list.stream().map(this::toDocumentResponse).toList();
    }

    @Transactional
    public void deleteDocument(UUID companyId, UUID documentId) {
        EmployeeDocument doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new AppException(OnboardingErrorCode.EMPLOYEE_DOCUMENT_NOT_FOUND));
        if (companyId != null && !doc.getCompanyId().equals(companyId)) {
            throw new AppException(OnboardingErrorCode.EMPLOYEE_DOCUMENT_NOT_FOUND);
        }
        documentRepository.delete(doc);
    }

    @Transactional
    public EmployeeDocumentResponse verifyDocument(UUID companyId, UUID documentId, UUID currentUserId) {
        EmployeeDocument doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new AppException(OnboardingErrorCode.EMPLOYEE_DOCUMENT_NOT_FOUND));
        if (companyId != null && !doc.getCompanyId().equals(companyId)) {
            throw new AppException(OnboardingErrorCode.EMPLOYEE_DOCUMENT_NOT_FOUND);
        }

        doc.setIsVerified(true);
        doc.setVerifiedAt(LocalDateTime.now());
        doc.setVerifiedByEmployeeId(resolveCurrentEmployeeId(currentUserId));
        EmployeeDocument saved = documentRepository.save(doc);
        return toDocumentResponse(saved);
    }

    // =========================================================================
    // HELPER & MAPPING METHODS
    // =========================================================================

    private OnboardingChecklistTemplate findTemplate(UUID companyId, UUID id) {
        if (companyId != null) {
            return templateRepository.findByIdAndCompanyId(id, companyId)
                    .orElseThrow(() -> new AppException(OnboardingErrorCode.CHECKLIST_TEMPLATE_NOT_FOUND));
        }
        return templateRepository.findById(id)
                .orElseThrow(() -> new AppException(OnboardingErrorCode.CHECKLIST_TEMPLATE_NOT_FOUND));
    }

    private OnboardingProcess findProcess(UUID companyId, UUID id) {
        if (companyId != null) {
            return processRepository.findByIdAndCompanyId(id, companyId)
                    .orElseThrow(() -> new AppException(OnboardingErrorCode.ONBOARDING_PROCESS_NOT_FOUND));
        }
        return processRepository.findById(id)
                .orElseThrow(() -> new AppException(OnboardingErrorCode.ONBOARDING_PROCESS_NOT_FOUND));
    }

    private UUID resolveCurrentEmployeeId(UUID currentUserId) {
        if (currentUserId == null) return null;
        return employeeService.findEmployeeIdByUserId(currentUserId).orElse(null);
    }

    private ChecklistTemplateDetailResponse toTemplateDetailResponse(OnboardingChecklistTemplate template) {
        ChecklistTemplateDetailResponse res = onboardingMapper.toDetailResponse(template);
        if (template.getApplicablePositionId() != null) {
            PositionSummary ps = positionService.getPositionSummary(template.getApplicablePositionId());
            if (ps != null) {
                res.setApplicablePosition(ps);
                res.setApplicablePositionTitle(ps.getName());
            }
        }
        if (template.getApplicableDepartmentId() != null) {
            OrgUnitSummary ds = orgUnitService.getUnitSummary(template.getApplicableDepartmentId());
            if (ds != null) {
                res.setApplicableDepartment(ds);
                res.setApplicableDepartmentName(ds.getName());
            }
        }
        if (template.getItems() != null) {
            res.setItems(template.getItems().stream()
                    .map(onboardingMapper::toItemResponse)
                    .toList());
        }
        return res;
    }

    private OnboardingProcessDetailResponse toProcessDetailResponse(OnboardingProcess process) {
        OnboardingProcessDetailResponse res = onboardingMapper.toProcessDetailResponse(process);

        // Employee and Department/Position info from EmployeeService
        EmployeeDetailResponse empDetail = employeeService.getEmployeeByIdInternal(process.getEmployeeId());
        if (empDetail != null) {
            res.setEmployeeDetail(empDetail);
            res.setEmployeeName(empDetail.getFullName());
            res.setEmployeeCode(empDetail.getEmployeeCode());
            res.setDepartment(empDetail.getOrganizationalUnit());
            res.setPosition(empDetail.getPosition());
            if (empDetail.getOrganizationalUnit() != null) {
                res.setDepartmentName(empDetail.getOrganizationalUnit().getName());
            }
            if (empDetail.getPosition() != null) {
                res.setPositionTitle(empDetail.getPosition().getName());
            }
        }

        templateRepository.findById(process.getChecklistTemplateId())
                .ifPresent(t -> res.setChecklistTemplateName(t.getName()));

        // Items with Batch Employee Enrichment
        List<OnboardingProcessItem> items = processItemRepository.findByOnboardingProcessIdOrderByOrderIndexAsc(process.getId());
        Set<UUID> empIds = items.stream().map(OnboardingProcessItem::getCompletedByEmployeeId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<UUID, EmployeeSummary> empMap = employeeService.getEmployeeSummaries(empIds);

        List<OnboardingProcessItemResponse> itemResponses = items.stream().map(i -> {
            OnboardingProcessItemResponse itemRes = onboardingMapper.toProcessItemResponse(i);
            if (i.getCompletedByEmployeeId() != null && empMap.containsKey(i.getCompletedByEmployeeId())) {
                itemRes.setCompletedByEmployeeName(empMap.get(i.getCompletedByEmployeeId()).getFullName());
            }
            return itemRes;
        }).toList();
        res.setItems(itemResponses);

        // Accounts
        List<AccountProvisioning> accounts = accountRepository.findByOnboardingProcessId(process.getId());
        res.setAccounts(accounts.stream().map(this::toAccountResponse).toList());

        // Orientation Sessions
        List<OrientationSession> sessions = sessionRepository.findByOnboardingProcessIdOrderByScheduledAtAsc(process.getId());
        res.setOrientationSessions(sessions.stream().map(this::toSessionResponse).toList());

        // Documents
        List<EmployeeDocument> documents = documentRepository.findByEmployeeIdOrderByUploadedAtDesc(process.getEmployeeId());
        res.setDocuments(documents.stream().map(this::toDocumentResponse).toList());

        // Allocated Assets via Public AssetService (Rule 1.2)
        try {
            PageData<AssetAllocationResponse> assetPage = assetService.getEmployeeAssets(process.getEmployeeId(), Pageable.unpaged());
            if (assetPage != null && assetPage.getItems() != null) {
                res.setAllocatedAssets(assetPage.getItems());
            }
        } catch (Exception e) {
            log.debug("No asset allocations retrieved: {}", e.getMessage());
        }

        // Progress
        res.setProgress(calculateProgress(process));

        return res;
    }

    private OnboardingProgressResponse calculateProgress(OnboardingProcess process) {
        long total = processItemRepository.countTotalByProcessId(process.getId());
        long completed = processItemRepository.countByProcessIdAndStatus(process.getId(), OnboardingItemStatus.COMPLETED);
        long skipped = processItemRepository.countByProcessIdAndStatus(process.getId(), OnboardingItemStatus.SKIPPED);
        long pending = processItemRepository.countByProcessIdAndStatus(process.getId(), OnboardingItemStatus.PENDING);
        long incompleteReq = processItemRepository.countIncompleteRequiredItems(process.getId());

        double percentage = total > 0 ? Math.round((double) completed / total * 100.0 * 10.0) / 10.0 : 0.0;
        boolean canComplete = (process.getStatus() == OnboardingStatus.IN_PROGRESS) && (incompleteReq == 0);

        EmployeeSummary emp = employeeService.getEmployeeSummary(process.getEmployeeId());
        String empName = emp != null ? emp.getFullName() : null;

        return OnboardingProgressResponse.builder()
                .onboardingProcessId(process.getId())
                .employeeId(process.getEmployeeId())
                .employeeName(empName)
                .status(process.getStatus())
                .totalItems(total)
                .completedItems(completed)
                .skippedItems(skipped)
                .pendingItems(pending)
                .incompleteRequiredItems(incompleteReq)
                .progressPercentage(percentage)
                .canComplete(canComplete)
                .build();
    }

    private AccountProvisioningResponse toAccountResponse(AccountProvisioning account) {
        AccountProvisioningResponse res = onboardingMapper.toAccountResponse(account);
        EmployeeSummary emp = employeeService.getEmployeeSummary(account.getEmployeeId());
        if (emp != null) {
            res.setEmployeeName(emp.getFullName());
            res.setEmployeeCode(emp.getEmployeeCode());
        }
        if (account.getProvisionedByEmployeeId() != null) {
            EmployeeSummary provBy = employeeService.getEmployeeSummary(account.getProvisionedByEmployeeId());
            if (provBy != null) {
                res.setProvisionedByEmployeeName(provBy.getFullName());
            }
        }
        return res;
    }

    private OrientationSessionResponse toSessionResponse(OrientationSession session) {
        OrientationSessionResponse res = onboardingMapper.toSessionResponse(session);
        if (session.getTrainerEmployeeId() != null) {
            EmployeeSummary trainer = employeeService.getEmployeeSummary(session.getTrainerEmployeeId());
            if (trainer != null) {
                res.setTrainerEmployeeName(trainer.getFullName());
            }
        }
        return res;
    }

    private EmployeeDocumentResponse toDocumentResponse(EmployeeDocument document) {
        EmployeeDocumentResponse res = onboardingMapper.toDocumentResponse(document);
        if (document.getVerifiedByEmployeeId() != null) {
            EmployeeSummary verifier = employeeService.getEmployeeSummary(document.getVerifiedByEmployeeId());
            if (verifier != null) {
                res.setVerifiedByEmployeeName(verifier.getFullName());
            }
        }
        return res;
    }
}
