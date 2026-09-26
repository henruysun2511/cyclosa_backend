package com.cyclosa.talent.service;

import com.cyclosa.common.dto.summary.EmployeeSummary;
import com.cyclosa.common.dto.summary.PositionSummary;
import com.cyclosa.common.exception.AppException;
import com.cyclosa.common.response.PageData;
import com.cyclosa.employee.dto.response.EmployeeDetailResponse;
import com.cyclosa.employee.service.EmployeeService;
import com.cyclosa.organization.service.PositionService;
import com.cyclosa.talent.dto.filter.SuccessionPlanFilter;
import com.cyclosa.talent.dto.request.AddSuccessionCandidateRequest;
import com.cyclosa.talent.dto.request.CreateSuccessionPlanRequest;
import com.cyclosa.talent.dto.request.UpdateSuccessionCandidateRequest;
import com.cyclosa.talent.dto.request.UpdateSuccessionPlanRequest;
import com.cyclosa.talent.dto.response.SuccessionCandidateResponse;
import com.cyclosa.talent.dto.response.SuccessionPlanDetailResponse;
import com.cyclosa.talent.dto.response.SuccessionPlanResponse;
import com.cyclosa.talent.entity.SuccessionCandidate;
import com.cyclosa.talent.entity.SuccessionPlan;
import com.cyclosa.talent.exception.TalentErrorCode;
import com.cyclosa.talent.mapper.TalentMapper;
import com.cyclosa.talent.repository.SuccessionCandidateRepository;
import com.cyclosa.talent.repository.SuccessionPlanRepository;
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
public class SuccessionPlanService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("createdAt", "reviewDate", "riskLevel");

    private final SuccessionPlanRepository successionPlanRepository;
    private final SuccessionCandidateRepository successionCandidateRepository;
    private final TalentMapper talentMapper;
    private final PositionService positionService;
    private final EmployeeService employeeService;

    @Transactional
    public SuccessionPlanResponse createSuccessionPlan(UUID companyId, CreateSuccessionPlanRequest request) {
        log.info("Creating succession plan in company id={} for position id={}", companyId, request.getPositionId());

        // Validate position exists via public service
        positionService.getPositionById(companyId, request.getPositionId());

        if (successionPlanRepository.existsByCompanyIdAndPositionId(companyId, request.getPositionId())) {
            throw new AppException(TalentErrorCode.SUCCESSION_PLAN_ALREADY_EXISTS);
        }

        SuccessionPlan plan = SuccessionPlan.builder()
                .companyId(companyId)
                .positionId(request.getPositionId())
                .riskLevel(request.getRiskLevel())
                .reviewDate(request.getReviewDate())
                .build();

        SuccessionPlan saved = successionPlanRepository.save(plan);
        return enrichPlanResponse(saved);
    }

    public PageData<SuccessionPlanResponse> getSuccessionPlans(UUID companyId, SuccessionPlanFilter filter) {
        Specification<SuccessionPlan> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("companyId"), companyId));

            if (filter.getPositionId() != null) {
                predicates.add(cb.equal(root.get("positionId"), filter.getPositionId()));
            }
            if (filter.getRiskLevel() != null) {
                predicates.add(cb.equal(root.get("riskLevel"), filter.getRiskLevel()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Pageable pageable = filter.toPageable("createdAt", ALLOWED_SORT_FIELDS);
        Page<SuccessionPlan> page = successionPlanRepository.findAll(spec, pageable);

        if (page.isEmpty()) {
            return PageData.empty(pageable);
        }

        List<SuccessionPlanResponse> responses = new ArrayList<>();
        Set<UUID> positionIds = new HashSet<>();
        for (SuccessionPlan plan : page.getContent()) {
            SuccessionPlanResponse res = talentMapper.toSuccessionPlanResponse(plan);
            res.setCandidateCount(plan.getCandidates() != null ? plan.getCandidates().size() : 0);
            responses.add(res);
            if (plan.getPositionId() != null) {
                positionIds.add(plan.getPositionId());
            }
        }

        if (!positionIds.isEmpty()) {
            Map<UUID, PositionSummary> posMap = positionService.getPositionSummaries(positionIds);
            responses.forEach(r -> r.setPosition(posMap.get(r.getPositionId())));
        }

        return PageData.of(page, responses);
    }

    public SuccessionPlanDetailResponse getSuccessionPlanDetail(UUID companyId, UUID id) {
        SuccessionPlan plan = successionPlanRepository.findByIdWithCandidates(id, companyId)
                .orElseThrow(() -> new AppException(TalentErrorCode.SUCCESSION_PLAN_NOT_FOUND));

        SuccessionPlanDetailResponse detail = talentMapper.toSuccessionPlanDetailResponse(plan);

        // Enrich position summary
        if (plan.getPositionId() != null) {
            PositionSummary posSummary = positionService.getPositionSummary(plan.getPositionId());
            detail.setPosition(posSummary);
        }

        // Enrich candidates
        if (plan.getCandidates() != null && !plan.getCandidates().isEmpty()) {
            List<SuccessionCandidateResponse> candidateResponses = talentMapper.toSuccessionCandidateResponseList(plan.getCandidates());
            Set<UUID> empIds = new HashSet<>();
            candidateResponses.forEach(c -> empIds.add(c.getEmployeeId()));

            Map<UUID, EmployeeSummary> empMap = employeeService.getEmployeeSummaries(empIds);
            candidateResponses.forEach(c -> c.setEmployee(empMap.get(c.getEmployeeId())));

            detail.setCandidates(candidateResponses);
        }

        return detail;
    }

    @Transactional
    public SuccessionPlanResponse updateSuccessionPlan(UUID companyId, UUID id, UpdateSuccessionPlanRequest request) {
        SuccessionPlan plan = successionPlanRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new AppException(TalentErrorCode.SUCCESSION_PLAN_NOT_FOUND));

        if (request.getRiskLevel() != null) {
            plan.setRiskLevel(request.getRiskLevel());
        }
        if (request.getReviewDate() != null) {
            plan.setReviewDate(request.getReviewDate());
        }

        SuccessionPlan saved = successionPlanRepository.save(plan);
        return enrichPlanResponse(saved);
    }

    @Transactional
    public void deleteSuccessionPlan(UUID companyId, UUID id) {
        SuccessionPlan plan = successionPlanRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new AppException(TalentErrorCode.SUCCESSION_PLAN_NOT_FOUND));
        successionPlanRepository.delete(plan);
        log.info("Deleted succession plan id={} in company id={}", id, companyId);
    }

    @Transactional
    public SuccessionCandidateResponse addCandidate(UUID companyId, UUID planId, AddSuccessionCandidateRequest request) {
        log.info("Adding candidate employee id={} to succession plan id={}", request.getEmployeeId(), planId);

        SuccessionPlan plan = successionPlanRepository.findByIdAndCompanyId(planId, companyId)
                .orElseThrow(() -> new AppException(TalentErrorCode.SUCCESSION_PLAN_NOT_FOUND));

        // Validate employee exists via public service
        EmployeeDetailResponse emp = employeeService.getEmployeeById(companyId, request.getEmployeeId());
        UUID candidatePosId = emp.getPosition() != null ? emp.getPosition().getId() : null;

        // Validate Rule 15: employee_id không được trùng với người đang giữ position_id của succession plan
        if (candidatePosId != null && candidatePosId.equals(plan.getPositionId())) {
            throw new AppException(TalentErrorCode.CANDIDATE_IS_CURRENT_HOLDER);
        }

        // Validate Rule 15: không được trùng lặp trong cùng 1 succession plan
        if (successionCandidateRepository.existsBySuccessionPlanIdAndEmployeeId(planId, request.getEmployeeId())) {
            throw new AppException(TalentErrorCode.CANDIDATE_ALREADY_ADDED);
        }

        SuccessionCandidate candidate = SuccessionCandidate.builder()
                .successionPlan(plan)
                .companyId(companyId)
                .employeeId(request.getEmployeeId())
                .readiness(request.getReadiness())
                .note(request.getNote())
                .build();

        SuccessionCandidate saved = successionCandidateRepository.save(candidate);
        SuccessionCandidateResponse res = talentMapper.toSuccessionCandidateResponse(saved);
        Map<UUID, EmployeeSummary> empMap = employeeService.getEmployeeSummaries(Set.of(request.getEmployeeId()));
        res.setEmployee(empMap.get(request.getEmployeeId()));
        return res;
    }

    @Transactional
    public SuccessionCandidateResponse updateCandidate(UUID companyId, UUID planId, UUID candidateId, UpdateSuccessionCandidateRequest request) {
        // Validate plan exists
        successionPlanRepository.findByIdAndCompanyId(planId, companyId)
                .orElseThrow(() -> new AppException(TalentErrorCode.SUCCESSION_PLAN_NOT_FOUND));

        SuccessionCandidate candidate = successionCandidateRepository.findBySuccessionPlanIdAndId(planId, candidateId)
                .orElseThrow(() -> new AppException(TalentErrorCode.SUCCESSION_CANDIDATE_NOT_FOUND));

        if (request.getReadiness() != null) {
            candidate.setReadiness(request.getReadiness());
        }
        if (request.getNote() != null) {
            candidate.setNote(request.getNote());
        }

        SuccessionCandidate saved = successionCandidateRepository.save(candidate);
        SuccessionCandidateResponse res = talentMapper.toSuccessionCandidateResponse(saved);
        Map<UUID, EmployeeSummary> empMap = employeeService.getEmployeeSummaries(Set.of(saved.getEmployeeId()));
        res.setEmployee(empMap.get(saved.getEmployeeId()));
        return res;
    }

    @Transactional
    public void removeCandidate(UUID companyId, UUID planId, UUID candidateId) {
        // Validate plan exists
        successionPlanRepository.findByIdAndCompanyId(planId, companyId)
                .orElseThrow(() -> new AppException(TalentErrorCode.SUCCESSION_PLAN_NOT_FOUND));

        SuccessionCandidate candidate = successionCandidateRepository.findBySuccessionPlanIdAndId(planId, candidateId)
                .orElseThrow(() -> new AppException(TalentErrorCode.SUCCESSION_CANDIDATE_NOT_FOUND));

        successionCandidateRepository.delete(candidate);
        log.info("Removed candidate id={} from succession plan id={}", candidateId, planId);
    }

    private SuccessionPlanResponse enrichPlanResponse(SuccessionPlan plan) {
        SuccessionPlanResponse res = talentMapper.toSuccessionPlanResponse(plan);
        res.setCandidateCount(plan.getCandidates() != null ? plan.getCandidates().size() : 0);
        if (plan.getPositionId() != null) {
            res.setPosition(positionService.getPositionSummary(plan.getPositionId()));
        }
        return res;
    }
}
