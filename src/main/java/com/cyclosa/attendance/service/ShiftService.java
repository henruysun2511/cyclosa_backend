package com.cyclosa.attendance.service;

import com.cyclosa.attendance.dto.request.CreateShiftRequest;
import com.cyclosa.attendance.dto.request.ShiftFilter;
import com.cyclosa.attendance.dto.request.UpdateShiftRequest;
import com.cyclosa.attendance.dto.response.ShiftResponse;
import com.cyclosa.attendance.entity.Shift;
import com.cyclosa.attendance.exception.AttendanceErrorCode;
import com.cyclosa.attendance.mapper.ShiftMapper;
import com.cyclosa.attendance.repository.ShiftAssignmentRepository;
import com.cyclosa.attendance.repository.ShiftRepository;
import com.cyclosa.common.dto.summary.ShiftSummary;
import com.cyclosa.common.exception.AppException;
import com.cyclosa.common.response.PageData;
import com.cyclosa.common.util.PageableUtils;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShiftService {

    private final ShiftRepository shiftRepository;
    private final ShiftAssignmentRepository shiftAssignmentRepository;
    private final com.cyclosa.organization.service.CompanyService companyService;
    private final ShiftMapper shiftMapper;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("name", "code", "startTime", "endTime", "createdAt");

    @Transactional(readOnly = true)
    public PageData<ShiftResponse> getShifts(UUID companyId, ShiftFilter filter, Pageable pageable) {
        UUID effectiveCompanyId = filter.getCompanyId() != null ? filter.getCompanyId() : companyId;
        if (effectiveCompanyId == null) {
            throw AppException.badRequest("Yêu cầu cung cấp ID công ty");
        }

        Specification<Shift> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("companyId"), effectiveCompanyId));

            if (filter.getIsActive() != null) {
                predicates.add(cb.equal(root.get("isActive"), filter.getIsActive()));
            }

            if (filter.getKeyword() != null && !filter.getKeyword().isBlank()) {
                String kw = "%" + PageableUtils.normalizeKeyword(filter.getKeyword()) + "%";
                Predicate nameLike = cb.like(cb.lower(root.get("name")), kw);
                Predicate codeLike = cb.like(cb.lower(root.get("code")), kw);
                predicates.add(cb.or(nameLike, codeLike));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Pageable effectivePageable = (pageable != null && pageable.isPaged()) ? pageable : filter.toPageable("createdAt", ALLOWED_SORT_FIELDS);
        Page<Shift> page = shiftRepository.findAll(spec, effectivePageable);
        return PageData.of(page, shiftMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public com.cyclosa.attendance.dto.response.ShiftDetailResponse getShiftById(UUID companyId, UUID id) {
        Shift shift = findShiftEntity(companyId, id);
        com.cyclosa.attendance.dto.response.ShiftDetailResponse res = shiftMapper.toDetailResponse(shift);
        if (shift.getCompanyId() != null) {
            res.setCompany(companyService.getCompanySummary(shift.getCompanyId()));
        }
        res.setActiveAssignmentsCount(shiftAssignmentRepository.countByShiftId(id));
        return res;
    }

    @Transactional
    public ShiftResponse createShift(UUID companyId, CreateShiftRequest request) {
        if (companyId == null) {
            throw AppException.badRequest("Yêu cầu cung cấp ID công ty");
        }

        validateShiftTimes(
                request.getStartTime(),
                request.getEndTime(),
                request.getBreakStartTime(),
                request.getBreakEndTime(),
                request.getIsNightShift(),
                request.getWorkingHours(),
                request.getWorkUnits(),
                request.getGraceLateMinutes(),
                request.getGraceEarlyMinutes()
        );

        if (shiftRepository.existsByCodeAndCompanyId(request.getCode(), companyId)) {
            throw new AppException(AttendanceErrorCode.SHIFT_CODE_EXISTS);
        }

        Shift shift = shiftMapper.toEntity(request);
        shift.setCompanyId(companyId);
        shift = shiftRepository.save(shift);
        log.info("Created Shift id={}, code={}, companyId={}", shift.getId(), shift.getCode(), companyId);

        return shiftMapper.toResponse(shift);
    }

    @Transactional
    public ShiftResponse updateShift(UUID companyId, UUID id, UpdateShiftRequest request) {
        Shift shift = findShiftEntity(companyId, id);

        LocalTime startTime = request.getStartTime() != null ? request.getStartTime() : shift.getStartTime();
        LocalTime endTime = request.getEndTime() != null ? request.getEndTime() : shift.getEndTime();
        LocalTime breakStart = request.getBreakStartTime() != null ? request.getBreakStartTime() : shift.getBreakStartTime();
        LocalTime breakEnd = request.getBreakEndTime() != null ? request.getBreakEndTime() : shift.getBreakEndTime();
        Boolean nightShift = request.getIsNightShift() != null ? request.getIsNightShift() : shift.getIsNightShift();
        BigDecimal hours = request.getWorkingHours() != null ? request.getWorkingHours() : shift.getWorkingHours();
        BigDecimal units = request.getWorkUnits() != null ? request.getWorkUnits() : shift.getWorkUnits();
        Integer graceLate = request.getGraceLateMinutes() != null ? request.getGraceLateMinutes() : shift.getGraceLateMinutes();
        Integer graceEarly = request.getGraceEarlyMinutes() != null ? request.getGraceEarlyMinutes() : shift.getGraceEarlyMinutes();

        validateShiftTimes(startTime, endTime, breakStart, breakEnd, nightShift, hours, units, graceLate, graceEarly);

        shiftMapper.updateEntityFromRequest(request, shift);
        shift = shiftRepository.save(shift);
        log.info("Updated Shift id={}", shift.getId());

        return shiftMapper.toResponse(shift);
    }

    private void validateShiftTimes(
            LocalTime startTime,
            LocalTime endTime,
            LocalTime breakStartTime,
            LocalTime breakEndTime,
            Boolean isNightShift,
            BigDecimal workingHours,
            BigDecimal workUnits,
            Integer graceLateMinutes,
            Integer graceEarlyMinutes
    ) {
        if (startTime == null || endTime == null) {
            throw AppException.badRequest("Giờ bắt đầu và kết thúc ca không được để trống");
        }
        boolean nightShift = Boolean.TRUE.equals(isNightShift);
        if (!nightShift && !endTime.isAfter(startTime)) {
            throw AppException.badRequest("Giờ kết thúc ca ngày phải sau giờ bắt đầu ca");
        }
        if (breakStartTime != null && breakEndTime != null && !breakEndTime.isAfter(breakStartTime)) {
            throw AppException.badRequest("Giờ kết thúc nghỉ phải sau giờ bắt đầu nghỉ");
        }
        if (workingHours != null && workingHours.compareTo(BigDecimal.ZERO) <= 0) {
            throw AppException.badRequest("Số giờ làm việc của ca phải lớn hơn 0");
        }
        if (workUnits != null && workUnits.compareTo(BigDecimal.ZERO) <= 0) {
            throw AppException.badRequest("Số công quy đổi phải lớn hơn 0");
        }
        if (graceLateMinutes != null && graceLateMinutes < 0) {
            throw AppException.badRequest("Số phút ân hạn đi muộn không được âm");
        }
        if (graceEarlyMinutes != null && graceEarlyMinutes < 0) {
            throw AppException.badRequest("Số phút ân hạn về sớm không được âm");
        }
    }

    @Transactional
    public void deleteShift(UUID companyId, UUID id) {
        Shift shift = findShiftEntity(companyId, id);

        if (shiftAssignmentRepository.existsByShiftId(id)) {
            throw new AppException(AttendanceErrorCode.SHIFT_HAS_ASSIGNMENTS);
        }

        shiftRepository.delete(shift);
        log.info("Deleted Shift id={}", id);
    }

    // --- Public Service Contracts ---

    @Transactional(readOnly = true)
    public ShiftSummary getShiftSummary(UUID shiftId) {
        if (shiftId == null) {
            return null;
        }
        return shiftRepository.findById(shiftId)
                .map(shiftMapper::toSummary)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public Map<UUID, ShiftSummary> getShiftSummaries(Set<UUID> shiftIds) {
        if (shiftIds == null || shiftIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return shiftRepository.findAllById(shiftIds).stream()
                .collect(Collectors.toMap(
                        Shift::getId,
                        shiftMapper::toSummary
                ));
    }

    @Transactional(readOnly = true)
    public Shift findShiftEntity(UUID companyId, UUID id) {
        Shift shift = shiftRepository.findById(id)
                .orElseThrow(() -> new AppException(AttendanceErrorCode.SHIFT_NOT_FOUND));

        if (companyId != null && !shift.getCompanyId().equals(companyId)) {
            throw new AppException(AttendanceErrorCode.SHIFT_NOT_FOUND);
        }
        return shift;
    }

    @Transactional(readOnly = true)
    public Shift getDefaultShift(UUID companyId) {
        return shiftRepository.findByCompanyIdAndCode(companyId, "CA_HANH_CHINH")
                .or(() -> shiftRepository.findFirstByCompanyIdAndIsActiveTrueOrderByCreatedAtAsc(companyId))
                .orElse(null);
    }
}
