package com.cyclosa.payroll.service;

import com.cyclosa.common.dto.summary.CompanySummary;
import com.cyclosa.common.exception.AppException;
import com.cyclosa.common.response.PageData;
import com.cyclosa.common.util.PageableUtils;
import com.cyclosa.organization.service.CompanyService;
import com.cyclosa.payroll.dto.request.CreateSalaryComponentRequest;
import com.cyclosa.payroll.dto.request.SalaryComponentFilter;
import com.cyclosa.payroll.dto.request.UpdateSalaryComponentRequest;
import com.cyclosa.payroll.dto.response.SalaryComponentDetailResponse;
import com.cyclosa.payroll.dto.response.SalaryComponentResponse;
import com.cyclosa.payroll.entity.SalaryComponent;
import com.cyclosa.payroll.exception.PayrollErrorCode;
import com.cyclosa.payroll.mapper.SalaryComponentMapper;
import com.cyclosa.payroll.repository.SalaryComponentRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SalaryComponentService {

    private final SalaryComponentRepository componentRepository;
    private final CompanyService companyService;
    private final SalaryComponentMapper componentMapper;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("code", "name", "componentType", "createdAt");

    @Transactional(readOnly = true)
    public PageData<SalaryComponentResponse> getComponents(UUID companyId, SalaryComponentFilter filter, Pageable pageable) {
        if (filter == null) {
            filter = new SalaryComponentFilter();
        }
        UUID effectiveCompanyId = filter.getCompanyId() != null ? filter.getCompanyId() : companyId;
        if (effectiveCompanyId == null) {
            throw AppException.badRequest("Yêu cầu cung cấp ID công ty");
        }
        filter.setCompanyId(effectiveCompanyId);

        final SalaryComponentFilter f = filter;
        Specification<SalaryComponent> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("companyId"), f.getCompanyId()));

            if (f.getComponentType() != null) {
                predicates.add(cb.equal(root.get("componentType"), f.getComponentType()));
            }
            if (f.getIsTaxable() != null) {
                predicates.add(cb.equal(root.get("isTaxable"), f.getIsTaxable()));
            }
            if (f.getIsInsuranceBase() != null) {
                predicates.add(cb.equal(root.get("isInsuranceBase"), f.getIsInsuranceBase()));
            }
            if (f.getKeyword() != null && !f.getKeyword().isBlank()) {
                String kw = "%" + PageableUtils.normalizeKeyword(f.getKeyword()) + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), kw),
                        cb.like(cb.lower(root.get("code")), kw)
                ));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Pageable effectivePageable = (pageable != null && pageable.isPaged()) ? pageable : filter.toPageable("createdAt", ALLOWED_SORT_FIELDS);
        Page<SalaryComponent> page = componentRepository.findAll(spec, effectivePageable);
        return PageData.of(page, componentMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public SalaryComponentDetailResponse getComponentById(UUID companyId, UUID id) {
        SalaryComponent component = componentRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new AppException(PayrollErrorCode.SALARY_COMPONENT_NOT_FOUND));

        SalaryComponentDetailResponse res = componentMapper.toDetailResponse(component);
        if (component.getCompanyId() != null) {
            CompanySummary companySummary = companyService.getCompanySummary(component.getCompanyId());
            res.setCompany(companySummary);
        }
        return res;
    }

    @Transactional
    public SalaryComponentResponse createComponent(UUID companyId, CreateSalaryComponentRequest request) {
        if (companyId == null) {
            throw AppException.badRequest("Yêu cầu cung cấp ID công ty");
        }
        if (componentRepository.existsByCodeAndCompanyId(request.getCode(), companyId)) {
            throw new AppException(PayrollErrorCode.SALARY_COMPONENT_CODE_EXISTS);
        }

        SalaryComponent component = componentMapper.toEntity(request);
        component.setCompanyId(companyId);
        component = componentRepository.save(component);
        log.info("Created SalaryComponent id={}, code={}, companyId={}", component.getId(), component.getCode(), companyId);
        return componentMapper.toResponse(component);
    }

    @Transactional
    public SalaryComponentResponse updateComponent(UUID companyId, UUID id, UpdateSalaryComponentRequest request) {
        SalaryComponent component = componentRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new AppException(PayrollErrorCode.SALARY_COMPONENT_NOT_FOUND));

        componentMapper.updateEntityFromRequest(request, component);
        component = componentRepository.save(component);
        log.info("Updated SalaryComponent id={}", component.getId());
        return componentMapper.toResponse(component);
    }

    @Transactional
    public void deleteComponent(UUID companyId, UUID id) {
        SalaryComponent component = componentRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new AppException(PayrollErrorCode.SALARY_COMPONENT_NOT_FOUND));
        componentRepository.delete(component);
        log.info("Deleted SalaryComponent id={}", id);
    }

    @Transactional(readOnly = true)
    public List<SalaryComponent> getActiveComponentsByCompany(UUID companyId) {
        return componentRepository.findAllByCompanyId(companyId);
    }
}
