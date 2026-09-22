package com.cyclosa.attendance.service;

import com.cyclosa.attendance.dto.request.BatchShiftAssignmentRequest;
import com.cyclosa.attendance.dto.request.ShiftAssignmentFilter;
import com.cyclosa.attendance.dto.response.ShiftAssignmentResponse;
import com.cyclosa.attendance.entity.Shift;
import com.cyclosa.attendance.entity.ShiftAssignment;
import com.cyclosa.attendance.enums.ShiftAssignmentStatus;
import com.cyclosa.attendance.exception.AttendanceErrorCode;
import com.cyclosa.attendance.mapper.ShiftAssignmentMapper;
import com.cyclosa.attendance.repository.ShiftAssignmentRepository;
import com.cyclosa.common.dto.summary.EmployeeSummary;
import com.cyclosa.common.enums.DataScope;
import com.cyclosa.common.exception.AppException;
import com.cyclosa.common.response.PageData;
import com.cyclosa.common.security.SecurityPermissionEvaluator;
import com.cyclosa.common.util.SecurityUtils;
import com.cyclosa.employee.dto.response.EmployeeDetailResponse;
import com.cyclosa.employee.service.EmployeeService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShiftAssignmentService {

    private final ShiftAssignmentRepository assignmentRepository;
    private final ShiftService shiftService;
    private final EmployeeService employeeService;
    private final SecurityPermissionEvaluator permEvaluator;
    private final ShiftAssignmentMapper assignmentMapper;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("assignedDate", "createdAt");

    @Transactional
    public List<ShiftAssignmentResponse> batchAssignShifts(UUID companyId, BatchShiftAssignmentRequest request) {
        if (companyId == null) {
            throw AppException.badRequest("Yêu cầu cung cấp ID công ty");
        }

        Shift shift = shiftService.findShiftEntity(companyId, request.getShiftId());

        if (request.getToDate().isBefore(request.getFromDate())) {
            throw AppException.badRequest("Ngày kết thúc phải sau hoặc bằng ngày bắt đầu");
        }

        List<ShiftAssignment> assignmentsToSave = new ArrayList<>();

        for (UUID employeeId : request.getEmployeeIds()) {
            LocalDate curr = request.getFromDate();
            while (!curr.isAfter(request.getToDate())) {
                if (request.getDaysOfWeek() == null || request.getDaysOfWeek().isEmpty()
                        || request.getDaysOfWeek().contains(curr.getDayOfWeek())) {

                    Optional<ShiftAssignment> existingOpt = assignmentRepository
                            .findByCompanyIdAndEmployeeIdAndAssignedDateWithShift(companyId, employeeId, curr);

                    ShiftAssignment assignment;
                    if (existingOpt.isPresent()) {
                        assignment = existingOpt.get();
                        assignment.setShift(shift);
                        assignment.setStatus(ShiftAssignmentStatus.ASSIGNED);
                        if (request.getNote() != null) {
                            assignment.setNote(request.getNote());
                        }
                    } else {
                        assignment = ShiftAssignment.builder()
                                .companyId(companyId)
                                .employeeId(employeeId)
                                .shift(shift)
                                .assignedDate(curr)
                                .status(ShiftAssignmentStatus.ASSIGNED)
                                .note(request.getNote())
                                .build();
                    }
                    assignmentsToSave.add(assignment);
                }
                curr = curr.plusDays(1);
            }
        }

        List<ShiftAssignment> saved = assignmentRepository.saveAll(assignmentsToSave);
        log.info("Batch assigned {} shifts for {} employees", saved.size(), request.getEmployeeIds().size());

        Map<UUID, EmployeeSummary> empSummaries = employeeService.getEmployeeSummaries(request.getEmployeeIds());
        return saved.stream().map(sa -> {
            ShiftAssignmentResponse res = assignmentMapper.toResponse(sa);
            res.setEmployee(empSummaries.get(sa.getEmployeeId()));
            return res;
        }).toList();
    }

    @Transactional(readOnly = true)
    public List<ShiftAssignmentResponse> getMySchedule(LocalDate fromDate, LocalDate toDate) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        UUID employeeId = employeeService.findEmployeeIdByUserId(currentUserId)
                .orElseThrow(() -> new AppException(AttendanceErrorCode.CURRENT_USER_NOT_EMPLOYEE));

        LocalDate start = fromDate != null ? fromDate : LocalDate.now().withDayOfMonth(1);
        LocalDate end = toDate != null ? toDate : start.plusMonths(1).minusDays(1);

        List<ShiftAssignment> assignments = assignmentRepository
                .findByEmployeeIdAndDateRangeWithShift(employeeId, start, end);

        EmployeeSummary empSummary = employeeService.getEmployeeSummary(employeeId);
        return assignments.stream().map(sa -> {
            ShiftAssignmentResponse res = assignmentMapper.toResponse(sa);
            res.setEmployee(empSummary);
            return res;
        }).toList();
    }

    @Transactional(readOnly = true)
    public PageData<ShiftAssignmentResponse> getShiftAssignments(UUID companyId, ShiftAssignmentFilter filter, Pageable pageable) {
        if (filter == null) {
            filter = new ShiftAssignmentFilter();
        }
        UUID effectiveCompanyId = filter.getCompanyId() != null ? filter.getCompanyId() : companyId;
        if (effectiveCompanyId == null) {
            throw AppException.badRequest("Yêu cầu cung cấp ID công ty");
        }
        filter.setCompanyId(effectiveCompanyId);

        applyDataScope(filter);

        final ShiftAssignmentFilter f = filter;
        Specification<ShiftAssignment> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("companyId"), f.getCompanyId()));

            if (f.getExactEmployeeId() != null) {
                predicates.add(cb.equal(root.get("employeeId"), f.getExactEmployeeId()));
            } else if (f.getAllowedEmployeeIds() != null) {
                predicates.add(root.get("employeeId").in(f.getAllowedEmployeeIds()));
            } else if (f.getEmployeeId() != null) {
                predicates.add(cb.equal(root.get("employeeId"), f.getEmployeeId()));
            }

            if (f.getShiftId() != null) {
                predicates.add(cb.equal(root.get("shift").get("id"), f.getShiftId()));
            }

            if (f.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), f.getStatus()));
            }

            if (f.getFromDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("assignedDate"), f.getFromDate()));
            }
            if (f.getToDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("assignedDate"), f.getToDate()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Pageable effectivePageable = (pageable != null && pageable.isPaged()) ? pageable : filter.toPageable("assignedDate", ALLOWED_SORT_FIELDS);
        Page<ShiftAssignment> page = assignmentRepository.findAll(spec, effectivePageable);
        if (page.isEmpty()) {
            return PageData.of(page, List.of());
        }

        Set<UUID> empIds = page.getContent().stream().map(ShiftAssignment::getEmployeeId).collect(Collectors.toSet());
        Map<UUID, EmployeeSummary> empMap = employeeService.getEmployeeSummaries(empIds);

        List<ShiftAssignmentResponse> list = page.getContent().stream().map(sa -> {
            ShiftAssignmentResponse res = assignmentMapper.toResponse(sa);
            res.setEmployee(empMap.get(sa.getEmployeeId()));
            return res;
        }).toList();

        return PageData.of(page, list);
    }

    @Transactional
    public void deleteAssignment(UUID companyId, UUID id) {
        ShiftAssignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new AppException(AttendanceErrorCode.SHIFT_ASSIGNMENT_NOT_FOUND));

        if (companyId != null && !assignment.getCompanyId().equals(companyId)) {
            throw new AppException(AttendanceErrorCode.SHIFT_ASSIGNMENT_NOT_FOUND);
        }

        assignmentRepository.delete(assignment);
        log.info("Deleted ShiftAssignment id={}", id);
    }

    @Transactional(readOnly = true)
    public Optional<ShiftAssignment> findAssignment(UUID companyId, UUID employeeId, LocalDate date) {
        return assignmentRepository.findByCompanyIdAndEmployeeIdAndAssignedDateWithShift(companyId, employeeId, date);
    }

    private void applyDataScope(ShiftAssignmentFilter filter) {
        DataScope scope = permEvaluator.getDataScope("attendance.schedule.view").orElse(DataScope.OWN);
        Optional<UUID> currentUserIdOpt = SecurityUtils.getCurrentUserIdOptional();

        if (scope == DataScope.ALL || scope == DataScope.COMPANY) {
            return;
        }

        if (currentUserIdOpt.isEmpty()) {
            filter.setExactEmployeeId(UUID.randomUUID());
            return;
        }

        UUID empId = employeeService.findEmployeeIdByUserId(currentUserIdOpt.get())
                .orElse(null);
        if (empId == null) {
            filter.setExactEmployeeId(UUID.randomUUID());
            return;
        }

        switch (scope) {
            case OWN -> filter.setExactEmployeeId(empId);
            case TEAM -> {
                Set<UUID> subordinateIds = employeeService.getSubordinateEmployeeIds(empId);
                filter.setAllowedEmployeeIds(subordinateIds);
            }
            case DEPARTMENT -> {
                EmployeeDetailResponse empDetail = employeeService.getEmployeeByIdInternal(empId);
                if (empDetail.getOrganizationalUnit() != null) {
                    Set<UUID> deptEmpIds = employeeService.getEmployeeIdsByDepartment(
                            empDetail.getCompany() != null ? empDetail.getCompany().getId() : null,
                            empDetail.getOrganizationalUnit().getId()
                    );
                    deptEmpIds.add(empId);
                    filter.setAllowedEmployeeIds(deptEmpIds);
                } else {
                    filter.setExactEmployeeId(empId);
                }
            }
            default -> filter.setExactEmployeeId(empId);
        }
    }
}
