package com.cyclosa.leave.service;

import com.cyclosa.common.exception.AppException;
import com.cyclosa.common.response.PageData;
import com.cyclosa.common.util.PageableUtils;
import com.cyclosa.leave.dto.request.CreateLeaveTypeRequest;
import com.cyclosa.leave.dto.request.LeaveTypeFilter;
import com.cyclosa.leave.dto.request.UpdateLeaveTypeRequest;
import com.cyclosa.leave.dto.response.LeaveTypeResponse;
import com.cyclosa.leave.entity.LeaveType;
import com.cyclosa.leave.exception.LeaveErrorCode;
import com.cyclosa.leave.mapper.LeaveTypeMapper;
import com.cyclosa.leave.repository.LeavePolicyRepository;
import com.cyclosa.leave.repository.LeaveRequestRepository;
import com.cyclosa.leave.repository.LeaveTypeRepository;
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
public class LeaveTypeService {

    private final LeaveTypeRepository leaveTypeRepository;
    private final LeavePolicyRepository leavePolicyRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveTypeMapper leaveTypeMapper;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("name", "code", "category", "createdAt");

    @Transactional(readOnly = true)
    public PageData<LeaveTypeResponse> getLeaveTypes(UUID companyId, LeaveTypeFilter filter, Pageable pageable) {
        if (filter == null) {
            filter = new LeaveTypeFilter();
        }
        UUID effectiveCompanyId = filter.getCompanyId() != null ? filter.getCompanyId() : companyId;

        final LeaveTypeFilter f = filter;
        Specification<LeaveType> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (effectiveCompanyId != null) {
                predicates.add(cb.or(
                        cb.equal(root.get("companyId"), effectiveCompanyId),
                        cb.isNull(root.get("companyId"))
                ));
            }
            if (f.getCategory() != null) {
                predicates.add(cb.equal(root.get("category"), f.getCategory()));
            }
            if (f.getFundingSource() != null) {
                predicates.add(cb.equal(root.get("fundingSource"), f.getFundingSource()));
            }
            if (f.getIsPaid() != null) {
                predicates.add(cb.equal(root.get("isPaid"), f.getIsPaid()));
            }
            if (f.getIsActive() != null) {
                predicates.add(cb.equal(root.get("isActive"), f.getIsActive()));
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
        Page<LeaveType> page = leaveTypeRepository.findAll(spec, effectivePageable);
        return PageData.of(page, leaveTypeMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public LeaveTypeResponse getLeaveTypeById(UUID companyId, UUID id) {
        LeaveType entity = leaveTypeRepository.findById(id)
                .orElseThrow(() -> new AppException(LeaveErrorCode.LEAVE_TYPE_NOT_FOUND));

        if (entity.getCompanyId() != null && companyId != null && !entity.getCompanyId().equals(companyId)) {
            throw new AppException(LeaveErrorCode.LEAVE_TYPE_NOT_FOUND);
        }
        return leaveTypeMapper.toResponse(entity);
    }

    @Transactional
    public LeaveTypeResponse createLeaveType(UUID companyId, CreateLeaveTypeRequest request) {
        if (companyId != null && leaveTypeRepository.existsByCodeAndCompanyId(request.getCode(), companyId)) {
            throw new AppException(LeaveErrorCode.LEAVE_TYPE_CODE_EXISTS);
        }

        LeaveType entity = leaveTypeMapper.toEntity(request);
        entity.setCompanyId(companyId);
        entity = leaveTypeRepository.save(entity);

        log.info("Created LeaveType id={}, code={}, companyId={}", entity.getId(), entity.getCode(), companyId);
        return leaveTypeMapper.toResponse(entity);
    }

    @Transactional
    public LeaveTypeResponse updateLeaveType(UUID companyId, UUID id, UpdateLeaveTypeRequest request) {
        LeaveType entity = leaveTypeRepository.findById(id)
                .orElseThrow(() -> new AppException(LeaveErrorCode.LEAVE_TYPE_NOT_FOUND));

        if (entity.getCompanyId() != null && companyId != null && !entity.getCompanyId().equals(companyId)) {
            throw new AppException(LeaveErrorCode.LEAVE_TYPE_NOT_FOUND);
        }

        leaveTypeMapper.updateEntityFromRequest(request, entity);
        entity = leaveTypeRepository.save(entity);

        log.info("Updated LeaveType id={}", id);
        return leaveTypeMapper.toResponse(entity);
    }

    @Transactional
    public void deleteLeaveType(UUID companyId, UUID id) {
        LeaveType entity = leaveTypeRepository.findById(id)
                .orElseThrow(() -> new AppException(LeaveErrorCode.LEAVE_TYPE_NOT_FOUND));

        if (entity.getCompanyId() != null && companyId != null && !entity.getCompanyId().equals(companyId)) {
            throw new AppException(LeaveErrorCode.LEAVE_TYPE_NOT_FOUND);
        }

        // Chặn xóa nếu đã có chính sách hoặc đơn xin nghỉ gắn với loại nghỉ này
        if (leavePolicyRepository.existsByLeaveTypeId(id) || leaveRequestRepository.existsByLeaveTypeId(id)) {
            throw new AppException(LeaveErrorCode.LEAVE_TYPE_IN_USE);
        }

        leaveTypeRepository.delete(entity);
        log.info("Deleted LeaveType id={}", id);
    }

    @Transactional(readOnly = true)
    public List<LeaveTypeResponse> getAllActiveLeaveTypes(UUID companyId) {
        List<LeaveType> list = leaveTypeRepository.findAllByCompanyIdOrCompanyIdIsNull(companyId)
                .stream()
                .filter(LeaveType::getIsActive)
                .toList();
        return leaveTypeMapper.toResponseList(list);
    }
}
