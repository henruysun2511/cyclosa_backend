package com.cyclosa.leave.service;

import com.cyclosa.common.exception.AppException;
import com.cyclosa.common.response.PageData;
import com.cyclosa.leave.dto.request.CreateLeavePolicyRequest;
import com.cyclosa.leave.dto.request.LeavePolicyFilter;
import com.cyclosa.leave.dto.request.UpdateLeavePolicyRequest;
import com.cyclosa.leave.dto.response.LeavePolicyResponse;
import com.cyclosa.leave.dto.response.LeaveTypeResponse;
import com.cyclosa.leave.entity.LeavePolicy;
import com.cyclosa.leave.enums.JobConditionLevel;
import com.cyclosa.leave.exception.LeaveErrorCode;
import com.cyclosa.leave.mapper.LeavePolicyMapper;
import com.cyclosa.leave.repository.LeavePolicyRepository;
import com.cyclosa.leave.repository.LeaveTypeRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeavePolicyService {

    private final LeavePolicyRepository policyRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final LeaveTypeService leaveTypeService;
    private final LeavePolicyMapper policyMapper;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("jobConditionLevel", "accrualDaysPerYear", "createdAt");

    @Transactional(readOnly = true)
    public PageData<LeavePolicyResponse> getPolicies(UUID companyId, LeavePolicyFilter filter, Pageable pageable) {
        if (filter == null) {
            filter = new LeavePolicyFilter();
        }
        UUID effectiveCompanyId = filter.getCompanyId() != null ? filter.getCompanyId() : companyId;
        if (effectiveCompanyId == null) {
            throw AppException.badRequest("Yêu cầu cung cấp ID công ty");
        }
        filter.setCompanyId(effectiveCompanyId);

        final LeavePolicyFilter f = filter;
        Specification<LeavePolicy> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("companyId"), f.getCompanyId()));

            if (f.getLeaveTypeId() != null) {
                predicates.add(cb.equal(root.get("leaveTypeId"), f.getLeaveTypeId()));
            }
            if (f.getApplicableEmploymentType() != null) {
                predicates.add(cb.equal(root.get("applicableEmploymentType"), f.getApplicableEmploymentType()));
            }
            if (f.getJobConditionLevel() != null) {
                predicates.add(cb.equal(root.get("jobConditionLevel"), f.getJobConditionLevel()));
            }
            if (f.getIsActive() != null) {
                predicates.add(cb.equal(root.get("isActive"), f.getIsActive()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Pageable effectivePageable = (pageable != null && pageable.isPaged()) ? pageable : filter.toPageable("createdAt", ALLOWED_SORT_FIELDS);
        Page<LeavePolicy> page = policyRepository.findAll(spec, effectivePageable);

        Set<UUID> typeIds = page.getContent().stream().map(LeavePolicy::getLeaveTypeId).collect(Collectors.toSet());
        Map<UUID, LeaveTypeResponse> typeMap = new HashMap<>();
        for (UUID tId : typeIds) {
            try {
                typeMap.put(tId, leaveTypeService.getLeaveTypeById(effectiveCompanyId, tId));
            } catch (Exception ignored) {}
        }

        List<LeavePolicyResponse> list = page.getContent().stream().map(p -> {
            LeavePolicyResponse res = policyMapper.toResponse(p);
            res.setLeaveType(typeMap.get(p.getLeaveTypeId()));
            return res;
        }).toList();

        return PageData.of(page, list);
    }

    @Transactional(readOnly = true)
    public LeavePolicyResponse getPolicyById(UUID companyId, UUID id) {
        LeavePolicy policy = policyRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new AppException(LeaveErrorCode.LEAVE_POLICY_NOT_FOUND));

        LeavePolicyResponse res = policyMapper.toResponse(policy);
        res.setLeaveType(leaveTypeService.getLeaveTypeById(companyId, policy.getLeaveTypeId()));
        return res;
    }

    @Transactional
    public LeavePolicyResponse createPolicy(UUID companyId, CreateLeavePolicyRequest request) {
        if (companyId == null) {
            throw AppException.badRequest("Yêu cầu cung cấp ID công ty");
        }

        // Kiểm tra loại phép tồn tại
        leaveTypeRepository.findById(request.getLeaveTypeId())
                .orElseThrow(() -> new AppException(LeaveErrorCode.LEAVE_TYPE_NOT_FOUND));

        if (policyRepository.existsByCompanyIdAndLeaveTypeIdAndJobConditionLevel(
                companyId, request.getLeaveTypeId(), request.getJobConditionLevel())) {
            throw new AppException(LeaveErrorCode.LEAVE_POLICY_EXISTS);
        }

        LeavePolicy policy = policyMapper.toEntity(request);
        policy.setCompanyId(companyId);
        policy = policyRepository.save(policy);

        log.info("Created LeavePolicy id={}, companyId={}, leaveTypeId={}", policy.getId(), companyId, policy.getLeaveTypeId());
        LeavePolicyResponse res = policyMapper.toResponse(policy);
        res.setLeaveType(leaveTypeService.getLeaveTypeById(companyId, policy.getLeaveTypeId()));
        return res;
    }

    @Transactional
    public LeavePolicyResponse updatePolicy(UUID companyId, UUID id, UpdateLeavePolicyRequest request) {
        LeavePolicy policy = policyRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new AppException(LeaveErrorCode.LEAVE_POLICY_NOT_FOUND));

        policyMapper.updateEntityFromRequest(request, policy);
        policy = policyRepository.save(policy);

        log.info("Updated LeavePolicy id={}", id);
        LeavePolicyResponse res = policyMapper.toResponse(policy);
        res.setLeaveType(leaveTypeService.getLeaveTypeById(companyId, policy.getLeaveTypeId()));
        return res;
    }

    @Transactional
    public void deletePolicy(UUID companyId, UUID id) {
        LeavePolicy policy = policyRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new AppException(LeaveErrorCode.LEAVE_POLICY_NOT_FOUND));

        policyRepository.delete(policy);
        log.info("Deleted LeavePolicy id={}", id);
    }

    @Transactional(readOnly = true)
    public Optional<LeavePolicy> findEffectivePolicy(UUID companyId, UUID leaveTypeId, JobConditionLevel conditionLevel) {
        return policyRepository.findByCompanyIdAndLeaveTypeIdAndJobConditionLevel(companyId, leaveTypeId, conditionLevel);
    }
}
