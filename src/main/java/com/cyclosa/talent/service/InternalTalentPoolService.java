package com.cyclosa.talent.service;

import com.cyclosa.common.dto.summary.EmployeeSummary;
import com.cyclosa.common.exception.AppException;
import com.cyclosa.common.response.PageData;
import com.cyclosa.employee.service.EmployeeService;
import com.cyclosa.talent.dto.filter.TalentPoolFilter;
import com.cyclosa.talent.dto.request.AddTalentPoolRequest;
import com.cyclosa.talent.dto.request.UpdateTalentPoolRequest;
import com.cyclosa.talent.dto.response.TalentPoolResponse;
import com.cyclosa.talent.entity.InternalTalentPool;
import com.cyclosa.talent.exception.TalentErrorCode;
import com.cyclosa.talent.mapper.TalentMapper;
import com.cyclosa.talent.repository.InternalTalentPoolRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InternalTalentPoolService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("createdAt", "tag");

    private final InternalTalentPoolRepository internalTalentPoolRepository;
    private final TalentMapper talentMapper;
    private final EmployeeService employeeService;

    @Transactional
    public TalentPoolResponse addTalent(UUID companyId, AddTalentPoolRequest request, UUID addedByEmployeeId) {
        log.info("Adding employee id={} to talent pool with tag={} in company id={}",
                request.getEmployeeId(), request.getTag(), companyId);

        // Validate employee exists via public service
        employeeService.getEmployeeById(companyId, request.getEmployeeId());

        if (internalTalentPoolRepository.existsByCompanyIdAndEmployeeIdAndTag(
                companyId, request.getEmployeeId(), request.getTag().trim())) {
            throw new AppException(TalentErrorCode.EMPLOYEE_ALREADY_IN_TALENT_POOL);
        }

        InternalTalentPool talent = InternalTalentPool.builder()
                .companyId(companyId)
                .employeeId(request.getEmployeeId())
                .tag(request.getTag().trim())
                .note(request.getNote())
                .addedByEmployeeId(addedByEmployeeId)
                .build();

        InternalTalentPool saved = internalTalentPoolRepository.save(talent);
        return enrichTalent(saved);
    }

    public PageData<TalentPoolResponse> getTalentPool(UUID companyId, TalentPoolFilter filter) {
        Specification<InternalTalentPool> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("companyId"), companyId));

            if (filter.getEmployeeId() != null) {
                predicates.add(cb.equal(root.get("employeeId"), filter.getEmployeeId()));
            }
            if (filter.getTag() != null && !filter.getTag().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("tag")), "%" + filter.getTag().trim().toLowerCase() + "%"));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Pageable pageable = filter.toPageable("createdAt", ALLOWED_SORT_FIELDS);
        Page<InternalTalentPool> page = internalTalentPoolRepository.findAll(spec, pageable);

        if (page.isEmpty()) {
            return PageData.empty(pageable);
        }

        List<TalentPoolResponse> responses = talentMapper.toTalentPoolResponseList(page.getContent());
        enrichEmployees(responses);
        return PageData.of(page, responses);
    }

    public TalentPoolResponse getTalentById(UUID companyId, UUID id) {
        InternalTalentPool talent = internalTalentPoolRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new AppException(TalentErrorCode.TALENT_POOL_RECORD_NOT_FOUND));
        return enrichTalent(talent);
    }

    @Transactional
    public TalentPoolResponse updateTalent(UUID companyId, UUID id, UpdateTalentPoolRequest request) {
        InternalTalentPool talent = internalTalentPoolRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new AppException(TalentErrorCode.TALENT_POOL_RECORD_NOT_FOUND));

        if (request.getTag() != null && !request.getTag().isBlank()) {
            talent.setTag(request.getTag().trim());
        }
        if (request.getNote() != null) {
            talent.setNote(request.getNote());
        }

        InternalTalentPool saved = internalTalentPoolRepository.save(talent);
        return enrichTalent(saved);
    }

    @Transactional
    public void deleteTalent(UUID companyId, UUID id) {
        InternalTalentPool talent = internalTalentPoolRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new AppException(TalentErrorCode.TALENT_POOL_RECORD_NOT_FOUND));
        internalTalentPoolRepository.delete(talent);
        log.info("Deleted talent pool entry id={} in company id={}", id, companyId);
    }

    private TalentPoolResponse enrichTalent(InternalTalentPool talent) {
        TalentPoolResponse res = talentMapper.toTalentPoolResponse(talent);
        enrichEmployees(List.of(res));
        return res;
    }

    private void enrichEmployees(List<TalentPoolResponse> responses) {
        Set<UUID> empIds = new HashSet<>();
        for (TalentPoolResponse res : responses) {
            if (res.getEmployeeId() != null) empIds.add(res.getEmployeeId());
            if (res.getAddedByEmployeeId() != null) empIds.add(res.getAddedByEmployeeId());
        }

        if (empIds.isEmpty()) return;

        Map<UUID, EmployeeSummary> map = employeeService.getEmployeeSummaries(empIds);
        for (TalentPoolResponse res : responses) {
            res.setEmployee(map.get(res.getEmployeeId()));
            res.setAddedByEmployee(map.get(res.getAddedByEmployeeId()));
        }
    }
}
