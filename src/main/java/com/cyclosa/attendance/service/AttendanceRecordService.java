package com.cyclosa.attendance.service;

import com.cyclosa.attendance.dto.request.AttendanceRecordFilter;
import com.cyclosa.attendance.dto.request.CheckInRequest;
import com.cyclosa.attendance.dto.request.CheckOutRequest;
import com.cyclosa.attendance.dto.response.AttendanceRecordResponse;
import com.cyclosa.attendance.dto.response.AttendanceTodayResponse;
import com.cyclosa.attendance.entity.AttendanceRecord;
import com.cyclosa.attendance.entity.Shift;
import com.cyclosa.attendance.entity.ShiftAssignment;
import com.cyclosa.attendance.enums.AttendanceStatus;
import com.cyclosa.attendance.enums.CheckMethod;
import com.cyclosa.attendance.exception.AttendanceErrorCode;
import com.cyclosa.attendance.mapper.AttendanceRecordMapper;
import com.cyclosa.attendance.mapper.ShiftMapper;
import com.cyclosa.attendance.repository.AttendanceRecordRepository;
import com.cyclosa.attendance.repository.ShiftAssignmentRepository;
import com.cyclosa.attendance.util.GeoDistanceUtils;
import com.cyclosa.attendance.util.WorkTimeCalculator;
import com.cyclosa.common.dto.summary.BranchSummary;
import com.cyclosa.common.dto.summary.EmployeeSummary;
import com.cyclosa.common.dto.summary.ShiftSummary;
import com.cyclosa.common.enums.DataScope;
import com.cyclosa.common.exception.AppException;
import com.cyclosa.common.response.PageData;
import com.cyclosa.common.security.SecurityPermissionEvaluator;
import com.cyclosa.common.util.SecurityUtils;
import com.cyclosa.employee.dto.response.EmployeeDetailResponse;
import com.cyclosa.employee.service.EmployeeService;
import com.cyclosa.organization.dto.response.BranchDetailResponse;
import com.cyclosa.organization.service.GeographyService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceRecordService {

    private final AttendanceRecordRepository recordRepository;
    private final ShiftAssignmentRepository shiftAssignmentRepository;
    private final ShiftService shiftService;
    private final ShiftAssignmentService assignmentService;
    private final EmployeeService employeeService;
    private final GeographyService geographyService;
    private final SecurityPermissionEvaluator permEvaluator;
    private final AttendanceRecordMapper recordMapper;
    private final ShiftMapper shiftMapper;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("workDate", "checkInTime", "checkOutTime", "createdAt");

    @Transactional
    public AttendanceRecordResponse checkIn(UUID currentUserId, CheckInRequest req) {
        UUID employeeId = employeeService.findEmployeeIdByUserId(currentUserId)
                .orElseThrow(() -> new AppException(AttendanceErrorCode.CURRENT_USER_NOT_EMPLOYEE));

        EmployeeDetailResponse emp = employeeService.getEmployeeByIdInternal(employeeId);
        UUID companyId = emp.getCompany() != null ? emp.getCompany().getId() : null;
        if (companyId == null) {
            throw AppException.badRequest("Không tìm thấy thông tin công ty của nhân sự");
        }

        LocalDate today = LocalDate.now();
        Optional<AttendanceRecord> existingOpt = recordRepository
                .findByCompanyIdAndEmployeeIdAndWorkDate(companyId, employeeId, today);

        if (existingOpt.isPresent() && existingOpt.get().getCheckInTime() != null) {
            throw new AppException(AttendanceErrorCode.ATTENDANCE_ALREADY_CHECKED_IN);
        }

        // 1. Kiểm tra tọa độ GPS đối với chi nhánh (nếu có GPS và chi nhánh)
        UUID branchId = emp.getBranch() != null ? emp.getBranch().getId() : null;
        if (branchId != null && req.getLatitude() != null && req.getLongitude() != null) {
            validateGpsLocation(companyId, branchId, req.getLatitude(), req.getLongitude());
        }

        // 2. Tìm ca làm việc của ngày
        Shift shift = null;
        if (req.getShiftId() != null) {
            shift = shiftService.findShiftEntity(companyId, req.getShiftId());
        } else {
            Optional<ShiftAssignment> assignmentOpt = assignmentService.findAssignment(companyId, employeeId, today);
            if (assignmentOpt.isPresent()) {
                shift = assignmentOpt.get().getShift();
            } else {
                shift = shiftService.getDefaultShift(companyId);
            }
        }

        LocalDateTime now = LocalDateTime.now();
        LocalTime startTime = shift != null ? shift.getStartTime() : LocalTime.of(8, 0);
        Integer graceLate = shift != null ? shift.getGraceLateMinutes() : 15;

        int lateMinutes = WorkTimeCalculator.calculateLateMinutes(now.toLocalTime(), startTime, graceLate);
        AttendanceStatus status = lateMinutes > 0 ? AttendanceStatus.LATE : AttendanceStatus.ON_TIME;

        AttendanceRecord record = existingOpt.orElseGet(() -> AttendanceRecord.builder()
                .companyId(companyId)
                .employeeId(employeeId)
                .workDate(today)
                .build());

        record.setBranchId(branchId);
        record.setShiftId(shift != null ? shift.getId() : null);
        record.setCheckInTime(now);
        record.setCheckInLat(req.getLatitude());
        record.setCheckInLong(req.getLongitude());
        record.setCheckInMethod(req.getMethod() != null ? req.getMethod() : CheckMethod.GPS);
        record.setLateMinutes(lateMinutes);
        record.setStatus(status);
        if (req.getNote() != null) {
            record.setNote(req.getNote());
        }

        record = recordRepository.save(record);
        log.info("Employee id={} checked in at {} (lateMinutes={}, status={})", employeeId, now, lateMinutes, status);

        return enrichRecordResponse(record, emp, shift);
    }

    @Transactional
    public AttendanceRecordResponse checkOut(UUID currentUserId, CheckOutRequest req) {
        UUID employeeId = employeeService.findEmployeeIdByUserId(currentUserId)
                .orElseThrow(() -> new AppException(AttendanceErrorCode.CURRENT_USER_NOT_EMPLOYEE));

        EmployeeDetailResponse emp = employeeService.getEmployeeByIdInternal(employeeId);
        UUID companyId = emp.getCompany() != null ? emp.getCompany().getId() : null;

        LocalDate today = LocalDate.now();
        Optional<AttendanceRecord> recordOpt = recordRepository.findByCompanyIdAndEmployeeIdAndWorkDate(companyId, employeeId, today);

        AttendanceRecord record;
        if (recordOpt.isPresent() && recordOpt.get().getCheckInTime() != null && recordOpt.get().getCheckOutTime() == null) {
            record = recordOpt.get();
        } else {
            // Check if yesterday has an unclosed check-in (e.g. night shift)
            LocalDate yesterday = today.minusDays(1);
            Optional<AttendanceRecord> yesterdayOpt = recordRepository
                    .findByCompanyIdAndEmployeeIdAndWorkDateAndCheckInTimeIsNotNullAndCheckOutTimeIsNull(companyId, employeeId, yesterday);
            if (yesterdayOpt.isPresent()) {
                record = yesterdayOpt.get();
            } else if (recordOpt.isPresent()) {
                record = recordOpt.get();
            } else {
                throw new AppException(AttendanceErrorCode.ATTENDANCE_NOT_CHECKED_IN);
            }
        }

        if (record.getCheckInTime() == null) {
            throw new AppException(AttendanceErrorCode.ATTENDANCE_NOT_CHECKED_IN);
        }
        if (record.getCheckOutTime() != null) {
            throw new AppException(AttendanceErrorCode.ATTENDANCE_ALREADY_CHECKED_OUT);
        }

        // 1. Kiểm tra tọa độ GPS đối với chi nhánh (nếu có GPS và chi nhánh)
        UUID branchId = record.getBranchId() != null ? record.getBranchId() : (emp.getBranch() != null ? emp.getBranch().getId() : null);
        if (branchId != null && req.getLatitude() != null && req.getLongitude() != null) {
            validateGpsLocation(companyId, branchId, req.getLatitude(), req.getLongitude());
        }

        // 2. Lấy thông tin ca làm việc
        Shift shift = null;
        if (record.getShiftId() != null) {
            shift = shiftService.findShiftEntity(companyId, record.getShiftId());
        } else {
            shift = shiftService.getDefaultShift(companyId);
        }

        LocalDateTime now = LocalDateTime.now();
        LocalTime endTime = shift != null ? shift.getEndTime() : LocalTime.of(17, 30);
        Integer graceEarly = shift != null ? shift.getGraceEarlyMinutes() : 0;
        LocalTime breakStart = shift != null ? shift.getBreakStartTime() : null;
        LocalTime breakEnd = shift != null ? shift.getBreakEndTime() : null;
        BigDecimal stdUnits = shift != null ? shift.getWorkUnits() : BigDecimal.ONE;

        int earlyMinutes = WorkTimeCalculator.calculateEarlyMinutes(now.toLocalTime(), endTime, graceEarly);
        BigDecimal actualHours = WorkTimeCalculator.calculateActualHours(record.getCheckInTime(), now, breakStart, breakEnd);
        BigDecimal actualWorkUnits = WorkTimeCalculator.calculateActualWorkUnits(actualHours, stdUnits);
        AttendanceStatus status = WorkTimeCalculator.resolveAttendanceStatus(true, true, record.getLateMinutes(), earlyMinutes);

        record.setCheckOutTime(now);
        record.setCheckOutLat(req.getLatitude());
        record.setCheckOutLong(req.getLongitude());
        record.setCheckOutMethod(req.getMethod() != null ? req.getMethod() : CheckMethod.GPS);
        record.setEarlyMinutes(earlyMinutes);
        record.setActualHours(actualHours);
        record.setActualWorkUnits(actualWorkUnits);
        record.setStatus(status);
        if (req.getNote() != null) {
            record.setNote(record.getNote() != null ? record.getNote() + " | " + req.getNote() : req.getNote());
        }

        record = recordRepository.save(record);
        log.info("Employee id={} checked out at {} (hours={}, units={}, status={})", employeeId, now, actualHours, actualWorkUnits, status);

        return enrichRecordResponse(record, emp, shift);
    }

    @Transactional(readOnly = true)
    public AttendanceTodayResponse getTodayAttendance(UUID currentUserId) {
        UUID employeeId = employeeService.findEmployeeIdByUserId(currentUserId)
                .orElseThrow(() -> new AppException(AttendanceErrorCode.CURRENT_USER_NOT_EMPLOYEE));

        EmployeeDetailResponse emp = employeeService.getEmployeeByIdInternal(employeeId);
        UUID companyId = emp.getCompany() != null ? emp.getCompany().getId() : null;
        LocalDate today = LocalDate.now();

        Optional<AttendanceRecord> recordOpt = recordRepository.findByCompanyIdAndEmployeeIdAndWorkDate(companyId, employeeId, today);

        Shift shift = null;
        if (recordOpt.isPresent() && recordOpt.get().getShiftId() != null) {
            shift = shiftService.findShiftEntity(companyId, recordOpt.get().getShiftId());
        } else {
            Optional<ShiftAssignment> saOpt = assignmentService.findAssignment(companyId, employeeId, today);
            if (saOpt.isPresent()) {
                shift = saOpt.get().getShift();
            } else if (companyId != null) {
                shift = shiftService.getDefaultShift(companyId);
            }
        }

        ShiftSummary shiftSummary = shift != null ? shiftMapper.toSummary(shift) : null;
        BranchSummary branchSummary = emp.getBranch();

        if (recordOpt.isPresent()) {
            AttendanceRecord record = recordOpt.get();
            AttendanceTodayResponse res = recordMapper.toTodayResponse(record);
            res.setShift(shiftSummary);
            res.setBranch(branchSummary);
            return res;
        }

        return AttendanceTodayResponse.builder()
                .workDate(today)
                .shift(shiftSummary)
                .branch(branchSummary)
                .lateMinutes(0)
                .earlyMinutes(0)
                .actualHours(BigDecimal.ZERO)
                .actualWorkUnits(BigDecimal.ZERO)
                .status(AttendanceStatus.ABSENT)
                .hasCheckedIn(false)
                .hasCheckedOut(false)
                .build();
    }

    @Transactional(readOnly = true)
    public List<AttendanceRecordResponse> getMyAttendanceHistory(UUID currentUserId, int month, int year) {
        UUID employeeId = employeeService.findEmployeeIdByUserId(currentUserId)
                .orElseThrow(() -> new AppException(AttendanceErrorCode.CURRENT_USER_NOT_EMPLOYEE));

        YearMonth ym = YearMonth.of(year, month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        List<AttendanceRecord> records = recordRepository
                .findAllByEmployeeIdAndWorkDateBetweenOrderByWorkDateAsc(employeeId, start, end);

        if (records.isEmpty()) {
            return List.of();
        }

        EmployeeSummary empSummary = employeeService.getEmployeeSummary(employeeId);
        Set<UUID> branchIds = records.stream().map(AttendanceRecord::getBranchId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<UUID> shiftIds = records.stream().map(AttendanceRecord::getShiftId).filter(Objects::nonNull).collect(Collectors.toSet());

        Map<UUID, BranchSummary> branchMap = geographyService.getBranchSummaries(branchIds);
        Map<UUID, ShiftSummary> shiftMap = shiftService.getShiftSummaries(shiftIds);

        return records.stream().map(r -> {
            AttendanceRecordResponse res = recordMapper.toResponse(r);
            res.setEmployee(empSummary);
            res.setBranch(branchMap.get(r.getBranchId()));
            res.setShift(shiftMap.get(r.getShiftId()));
            return res;
        }).toList();
    }

    @Transactional(readOnly = true)
    public PageData<AttendanceRecordResponse> getAttendanceRecords(UUID companyId, AttendanceRecordFilter filter, Pageable pageable) {
        if (filter == null) {
            filter = new AttendanceRecordFilter();
        }
        UUID effectiveCompanyId = filter.getCompanyId() != null ? filter.getCompanyId() : companyId;
        if (effectiveCompanyId == null) {
            throw AppException.badRequest("Yêu cầu cung cấp ID công ty");
        }
        filter.setCompanyId(effectiveCompanyId);

        applyDataScope(filter);

        final AttendanceRecordFilter f = filter;
        Specification<AttendanceRecord> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("companyId"), f.getCompanyId()));

            if (f.getExactEmployeeId() != null) {
                predicates.add(cb.equal(root.get("employeeId"), f.getExactEmployeeId()));
            } else if (f.getAllowedEmployeeIds() != null) {
                predicates.add(root.get("employeeId").in(f.getAllowedEmployeeIds()));
            } else if (f.getEmployeeId() != null) {
                predicates.add(cb.equal(root.get("employeeId"), f.getEmployeeId()));
            }

            if (f.getBranchId() != null) {
                predicates.add(cb.equal(root.get("branchId"), f.getBranchId()));
            }
            if (f.getShiftId() != null) {
                predicates.add(cb.equal(root.get("shiftId"), f.getShiftId()));
            }
            if (f.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), f.getStatus()));
            }
            if (f.getFromDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("workDate"), f.getFromDate()));
            }
            if (f.getToDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("workDate"), f.getToDate()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Pageable effectivePageable = (pageable != null && pageable.isPaged()) ? pageable : filter.toPageable("workDate", ALLOWED_SORT_FIELDS);
        Page<AttendanceRecord> page = recordRepository.findAll(spec, effectivePageable);
        if (page.isEmpty()) {
            return PageData.of(page, List.of());
        }

        Set<UUID> empIds = page.getContent().stream().map(AttendanceRecord::getEmployeeId).collect(Collectors.toSet());
        Set<UUID> branchIds = page.getContent().stream().map(AttendanceRecord::getBranchId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<UUID> shiftIds = page.getContent().stream().map(AttendanceRecord::getShiftId).filter(Objects::nonNull).collect(Collectors.toSet());

        Map<UUID, EmployeeSummary> empMap = employeeService.getEmployeeSummaries(empIds);
        Map<UUID, BranchSummary> branchMap = geographyService.getBranchSummaries(branchIds);
        Map<UUID, ShiftSummary> shiftMap = shiftService.getShiftSummaries(shiftIds);

        List<AttendanceRecordResponse> list = page.getContent().stream().map(r -> {
            AttendanceRecordResponse res = recordMapper.toResponse(r);
            res.setEmployee(empMap.get(r.getEmployeeId()));
            res.setBranch(branchMap.get(r.getBranchId()));
            res.setShift(shiftMap.get(r.getShiftId()));
            return res;
        }).toList();

        return PageData.of(page, list);
    }

    private void validateGpsLocation(UUID companyId, UUID branchId, Double userLat, Double userLong) {
        try {
            BranchDetailResponse branch = geographyService.getBranchById(companyId, branchId);
            if (branch.getLatitude() != null && branch.getLongitude() != null) {
                double dist = GeoDistanceUtils.calculateDistanceMeters(
                        userLat, userLong,
                        branch.getLatitude().doubleValue(), branch.getLongitude().doubleValue()
                );
                int radius = branch.getCheckinRadiusMeters() != null ? branch.getCheckinRadiusMeters() : 200;
                if (dist > radius) {
                    throw new AppException(AttendanceErrorCode.LOCATION_OUT_OF_RANGE,
                            String.format("Vị trí chấm công cách chi nhánh %.1f mét, vượt quá bán kính cho phép (%d mét)", dist, radius));
                }
            }
        } catch (AppException e) {
            throw e;
        } catch (Exception ex) {
            log.warn("Không thể xác thực tọa độ GPS chi nhánh: {}", ex.getMessage());
        }
    }

    private AttendanceRecordResponse enrichRecordResponse(AttendanceRecord record, EmployeeDetailResponse emp, Shift shift) {
        AttendanceRecordResponse res = recordMapper.toResponse(record);
        res.setEmployee(employeeService.getEmployeeSummary(record.getEmployeeId()));
        if (emp != null && emp.getBranch() != null) {
            res.setBranch(emp.getBranch());
        }
        if (shift != null) {
            res.setShift(shiftMapper.toSummary(shift));
        }
        return res;
    }

    private void applyDataScope(AttendanceRecordFilter filter) {
        DataScope scope = permEvaluator.getDataScope("attendance.record.view").orElse(DataScope.OWN);
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

    @Transactional
    public void sweepDailyAttendanceRecords(LocalDate date) {
        log.info("Starting daily attendance sweep for date: {}", date);

        // 1. Quét các lượt check-in quên check-out -> MISSING_CHECK_OUT
        List<AttendanceRecord> unclosedRecords = recordRepository
                .findAllByWorkDateAndCheckInTimeIsNotNullAndCheckOutTimeIsNull(date);
        for (AttendanceRecord r : unclosedRecords) {
            r.setStatus(AttendanceStatus.MISSING_CHECK_OUT);
            r.setActualHours(BigDecimal.ZERO);
            r.setActualWorkUnits(BigDecimal.ZERO);
        }
        if (!unclosedRecords.isEmpty()) {
            recordRepository.saveAll(unclosedRecords);
            log.info("Marked {} unclosed attendance records as MISSING_CHECK_OUT for date {}", unclosedRecords.size(), date);
        }

        // 2. Quét các ca được phân (ShiftAssignment) mà nhân viên không đi làm -> ABSENT
        List<ShiftAssignment> assignments = shiftAssignmentRepository.findAllByAssignedDate(date);
        List<AttendanceRecord> existingRecords = recordRepository.findAllByWorkDate(date);
        Set<UUID> employeesWithAttendance = existingRecords.stream()
                .map(AttendanceRecord::getEmployeeId)
                .collect(Collectors.toSet());

        List<AttendanceRecord> absentRecords = new ArrayList<>();
        for (ShiftAssignment sa : assignments) {
            if (!employeesWithAttendance.contains(sa.getEmployeeId())) {
                absentRecords.add(AttendanceRecord.builder()
                        .companyId(sa.getCompanyId())
                        .employeeId(sa.getEmployeeId())
                        .shiftId(sa.getShift() != null ? sa.getShift().getId() : null)
                        .workDate(date)
                        .status(AttendanceStatus.ABSENT)
                        .lateMinutes(0)
                        .earlyMinutes(0)
                        .actualHours(BigDecimal.ZERO)
                        .actualWorkUnits(BigDecimal.ZERO)
                        .build());
                employeesWithAttendance.add(sa.getEmployeeId());
            }
        }
        if (!absentRecords.isEmpty()) {
            recordRepository.saveAll(absentRecords);
            log.info("Created {} ABSENT attendance records for date {}", absentRecords.size(), date);
        }
    }
}
