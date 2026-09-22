package com.cyclosa.attendance.service;

import com.cyclosa.attendance.dto.request.LockTimesheetRequest;
import com.cyclosa.attendance.dto.request.RecalculateTimesheetRequest;
import com.cyclosa.attendance.dto.request.TimesheetFilter;
import com.cyclosa.attendance.dto.response.MonthlyTimesheetResponse;
import com.cyclosa.attendance.entity.AttendanceRecord;
import com.cyclosa.attendance.entity.MonthlyTimesheet;
import com.cyclosa.attendance.enums.AttendanceStatus;
import com.cyclosa.attendance.exception.AttendanceErrorCode;
import com.cyclosa.attendance.mapper.TimesheetMapper;
import com.cyclosa.attendance.repository.AttendanceRecordRepository;
import com.cyclosa.attendance.repository.MonthlyTimesheetRepository;
import com.cyclosa.common.dto.summary.EmployeeAttendanceSummary;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TimesheetService {

    private final MonthlyTimesheetRepository timesheetRepository;
    private final AttendanceRecordRepository recordRepository;
    private final EmployeeService employeeService;
    private final SecurityPermissionEvaluator permEvaluator;
    private final TimesheetMapper timesheetMapper;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("year", "month", "actualWorkDays", "createdAt");

    @Transactional(readOnly = true)
    public MonthlyTimesheetResponse getMyTimesheet(int month, int year) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        UUID employeeId = employeeService.findEmployeeIdByUserId(currentUserId)
                .orElseThrow(() -> new AppException(AttendanceErrorCode.CURRENT_USER_NOT_EMPLOYEE));

        MonthlyTimesheet timesheet = timesheetRepository
                .findByEmployeeIdAndMonthAndYear(employeeId, month, year)
                .orElseGet(() -> calculateTimesheetSnapshot(null, employeeId, month, year));

        MonthlyTimesheetResponse res = timesheetMapper.toResponse(timesheet);
        res.setEmployee(employeeService.getEmployeeSummary(employeeId));
        return res;
    }

    @Transactional(readOnly = true)
    public PageData<MonthlyTimesheetResponse> getTimesheetSummary(UUID companyId, TimesheetFilter filter, Pageable pageable) {
        if (filter == null) {
            filter = new TimesheetFilter();
        }
        UUID effectiveCompanyId = filter.getCompanyId() != null ? filter.getCompanyId() : companyId;
        if (effectiveCompanyId == null) {
            throw AppException.badRequest("Yêu cầu cung cấp ID công ty");
        }
        filter.setCompanyId(effectiveCompanyId);

        applyDataScope(filter);

        final TimesheetFilter f = filter;
        Specification<MonthlyTimesheet> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("companyId"), f.getCompanyId()));

            if (f.getExactEmployeeId() != null) {
                predicates.add(cb.equal(root.get("employeeId"), f.getExactEmployeeId()));
            } else if (f.getAllowedEmployeeIds() != null) {
                predicates.add(root.get("employeeId").in(f.getAllowedEmployeeIds()));
            } else if (f.getEmployeeId() != null) {
                predicates.add(cb.equal(root.get("employeeId"), f.getEmployeeId()));
            }

            if (f.getMonth() != null) {
                predicates.add(cb.equal(root.get("month"), f.getMonth()));
            }
            if (f.getYear() != null) {
                predicates.add(cb.equal(root.get("year"), f.getYear()));
            }
            if (f.getIsLocked() != null) {
                predicates.add(cb.equal(root.get("isLocked"), f.getIsLocked()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Pageable effectivePageable = (pageable != null && pageable.isPaged()) ? pageable : filter.toPageable("createdAt", ALLOWED_SORT_FIELDS);
        Page<MonthlyTimesheet> page = timesheetRepository.findAll(spec, effectivePageable);
        if (page.isEmpty()) {
            return PageData.of(page, List.of());
        }

        Set<UUID> empIds = page.getContent().stream().map(MonthlyTimesheet::getEmployeeId).collect(Collectors.toSet());
        Map<UUID, EmployeeSummary> empMap = employeeService.getEmployeeSummaries(empIds);

        List<MonthlyTimesheetResponse> list = page.getContent().stream().map(ts -> {
            MonthlyTimesheetResponse res = timesheetMapper.toResponse(ts);
            res.setEmployee(empMap.get(ts.getEmployeeId()));
            return res;
        }).toList();

        return PageData.of(page, list);
    }

    @Transactional
    public List<MonthlyTimesheetResponse> recalculateTimesheets(UUID companyId, RecalculateTimesheetRequest req) {
        if (companyId == null) {
            throw AppException.badRequest("Yêu cầu cung cấp ID công ty");
        }

        Set<UUID> employeeIds = req.getEmployeeIds();
        if (employeeIds == null || employeeIds.isEmpty()) {
            // Lấy tất cả records của công ty trong tháng để trích xuất danh sách nhân viên có công
            YearMonth ym = YearMonth.of(req.getYear(), req.getMonth());
            List<AttendanceRecord> records = recordRepository
                    .findAllByCompanyIdAndWorkDateBetween(companyId, ym.atDay(1), ym.atEndOfMonth());
            employeeIds = records.stream().map(AttendanceRecord::getEmployeeId).collect(Collectors.toSet());
        }

        if (employeeIds.isEmpty()) {
            return List.of();
        }

        List<MonthlyTimesheet> updatedList = new ArrayList<>();
        for (UUID empId : employeeIds) {
            MonthlyTimesheet ts = recalculateTimesheetForEmployee(companyId, empId, req.getMonth(), req.getYear());
            if (ts != null) {
                updatedList.add(ts);
            }
        }

        Map<UUID, EmployeeSummary> empMap = employeeService.getEmployeeSummaries(employeeIds);
        return updatedList.stream().map(ts -> {
            MonthlyTimesheetResponse res = timesheetMapper.toResponse(ts);
            res.setEmployee(empMap.get(ts.getEmployeeId()));
            return res;
        }).toList();
    }

    @Transactional
    public MonthlyTimesheet recalculateTimesheetForEmployee(UUID companyId, UUID employeeId, int month, int year) {
        Optional<MonthlyTimesheet> existingOpt = timesheetRepository
                .findByCompanyIdAndEmployeeIdAndMonthAndYear(companyId, employeeId, month, year);

        if (existingOpt.isPresent() && Boolean.TRUE.equals(existingOpt.get().getIsLocked())) {
            log.warn("Bảng công của employeeId={} tháng {}/{} đã bị khóa, bỏ qua tính toán lại", employeeId, month, year);
            return existingOpt.get();
        }

        MonthlyTimesheet ts = calculateTimesheetSnapshot(companyId, employeeId, month, year);
        if (existingOpt.isPresent()) {
            MonthlyTimesheet existing = existingOpt.get();
            existing.setActualWorkDays(ts.getActualWorkDays());
            existing.setStandardWorkDays(ts.getStandardWorkDays());
            existing.setTotalPaidDays(ts.getTotalPaidDays());
            existing.setTotalLateMinutes(ts.getTotalLateMinutes());
            existing.setTotalEarlyMinutes(ts.getTotalEarlyMinutes());
            existing.setMissingPunchCount(ts.getMissingPunchCount());
            return timesheetRepository.save(existing);
        } else {
            return timesheetRepository.save(ts);
        }
    }

    @Transactional
    public void lockTimesheet(UUID companyId, LockTimesheetRequest req) {
        if (companyId == null) {
            throw AppException.badRequest("Yêu cầu cung cấp ID công ty");
        }

        YearMonth ym = YearMonth.of(req.getYear(), req.getMonth());
        List<AttendanceRecord> records = recordRepository
                .findAllByCompanyIdAndWorkDateBetween(companyId, ym.atDay(1), ym.atEndOfMonth());
        Set<UUID> empIdsWithAttendance = records.stream().map(AttendanceRecord::getEmployeeId).collect(Collectors.toSet());
        for (UUID empId : empIdsWithAttendance) {
            recalculateTimesheetForEmployee(companyId, empId, req.getMonth(), req.getYear());
        }

        List<MonthlyTimesheet> timesheets = timesheetRepository
                .findAllByCompanyIdAndMonthAndYear(companyId, req.getMonth(), req.getYear());

        for (MonthlyTimesheet ts : timesheets) {
            ts.setIsLocked(true);
        }
        timesheetRepository.saveAll(timesheets);
        log.info("Locked timesheets for companyId={}, month={}/{} (count={})", companyId, req.getMonth(), req.getYear(), timesheets.size());
    }

    // --- Public Service Contract cho Module 08 (Payroll) ---

    @Transactional(readOnly = true)
    public Map<UUID, EmployeeAttendanceSummary> getMonthlyAttendanceSummaries(
            UUID companyId, int month, int year, Set<UUID> employeeIds
    ) {
        if (companyId == null) {
            return Collections.emptyMap();
        }

        List<MonthlyTimesheet> timesheets;
        if (employeeIds != null && !employeeIds.isEmpty()) {
            timesheets = timesheetRepository.findAllByCompanyIdAndEmployeeIdInAndMonthAndYear(companyId, employeeIds, month, year);
        } else {
            timesheets = timesheetRepository.findAllByCompanyIdAndMonthAndYear(companyId, month, year);
        }

        Map<UUID, MonthlyTimesheet> map = timesheets.stream()
                .collect(Collectors.toMap(MonthlyTimesheet::getEmployeeId, ts -> ts, (a, b) -> a));

        Map<UUID, EmployeeAttendanceSummary> result = new HashMap<>();
        Set<UUID> targetIds = (employeeIds != null && !employeeIds.isEmpty()) ? employeeIds : map.keySet();

        for (UUID empId : targetIds) {
            MonthlyTimesheet ts = map.get(empId);
            if (ts == null) {
                ts = calculateTimesheetSnapshot(companyId, empId, month, year);
            }
            result.put(empId, EmployeeAttendanceSummary.builder()
                    .employeeId(empId)
                    .month(month)
                    .year(year)
                    .standardWorkDays(ts.getStandardWorkDays())
                    .actualWorkDays(ts.getActualWorkDays())
                    .paidLeaveDays(ts.getPaidLeaveDays())
                    .totalPaidWorkDays(ts.getTotalPaidDays())
                    .totalLateMinutes(ts.getTotalLateMinutes())
                    .totalEarlyMinutes(ts.getTotalEarlyMinutes())
                    .missingPunchCount(ts.getMissingPunchCount())
                    .build());
        }

        return result;
    }

    private MonthlyTimesheet calculateTimesheetSnapshot(UUID companyId, UUID employeeId, int month, int year) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        List<AttendanceRecord> records = recordRepository
                .findAllByEmployeeIdAndWorkDateBetweenOrderByWorkDateAsc(employeeId, start, end);

        BigDecimal actualWorkDays = BigDecimal.ZERO;
        int totalLate = 0;
        int totalEarly = 0;
        int missingPunch = 0;

        for (AttendanceRecord r : records) {
            if (r.getActualWorkUnits() != null) {
                actualWorkDays = actualWorkDays.add(r.getActualWorkUnits());
            }
            if (r.getLateMinutes() != null) {
                totalLate += r.getLateMinutes();
            }
            if (r.getEarlyMinutes() != null) {
                totalEarly += r.getEarlyMinutes();
            }
            if (r.getStatus() == AttendanceStatus.MISSING_CHECK_OUT) {
                missingPunch++;
            }
        }

        int standardDaysCount = countStandardWorkDays(year, month);
        BigDecimal standardWorkDays = BigDecimal.valueOf(standardDaysCount).setScale(2, RoundingMode.HALF_UP);
        BigDecimal paidLeaveDays = BigDecimal.ZERO; // Sau này sẽ lấy thêm từ Module Leave
        BigDecimal totalPaidDays = actualWorkDays.add(paidLeaveDays);

        UUID effectiveCompanyId = companyId;
        if (effectiveCompanyId == null && !records.isEmpty()) {
            effectiveCompanyId = records.get(0).getCompanyId();
        }

        return MonthlyTimesheet.builder()
                .companyId(effectiveCompanyId)
                .employeeId(employeeId)
                .month(month)
                .year(year)
                .standardWorkDays(standardWorkDays)
                .actualWorkDays(actualWorkDays.setScale(2, RoundingMode.HALF_UP))
                .paidLeaveDays(paidLeaveDays)
                .unpaidLeaveDays(BigDecimal.ZERO)
                .totalPaidDays(totalPaidDays.setScale(2, RoundingMode.HALF_UP))
                .totalLateMinutes(totalLate)
                .totalEarlyMinutes(totalEarly)
                .missingPunchCount(missingPunch)
                .isLocked(false)
                .build();
    }

    private int countStandardWorkDays(int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        int count = 0;
        for (int day = 1; day <= ym.lengthOfMonth(); day++) {
            DayOfWeek dow = ym.atDay(day).getDayOfWeek();
            if (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY) {
                count++;
            }
        }
        return count;
    }

    private void applyDataScope(TimesheetFilter filter) {
        DataScope scope = permEvaluator.getDataScope("attendance.timesheet.view").orElse(DataScope.OWN);
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
