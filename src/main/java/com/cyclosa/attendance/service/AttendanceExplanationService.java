package com.cyclosa.attendance.service;

import com.cyclosa.attendance.dto.request.CreateExplanationRequest;
import com.cyclosa.attendance.dto.request.ExplanationFilter;
import com.cyclosa.attendance.dto.response.AttendanceExplanationResponse;
import com.cyclosa.attendance.entity.AttendanceExplanation;
import com.cyclosa.attendance.entity.AttendanceRecord;
import com.cyclosa.attendance.entity.Shift;
import com.cyclosa.attendance.entity.ShiftAssignment;
import com.cyclosa.attendance.enums.AttendanceStatus;
import com.cyclosa.attendance.enums.CheckMethod;
import com.cyclosa.attendance.enums.ExplanationStatus;
import com.cyclosa.attendance.exception.AttendanceErrorCode;
import com.cyclosa.attendance.mapper.AttendanceExplanationMapper;
import com.cyclosa.attendance.repository.AttendanceExplanationRepository;
import com.cyclosa.attendance.repository.AttendanceRecordRepository;
import com.cyclosa.attendance.util.WorkTimeCalculator;
import com.cyclosa.common.dto.summary.EmployeeSummary;
import com.cyclosa.common.enums.DataScope;
import com.cyclosa.common.exception.AppException;
import com.cyclosa.common.response.PageData;
import com.cyclosa.common.security.SecurityPermissionEvaluator;
import com.cyclosa.common.util.SecurityUtils;
import com.cyclosa.employee.dto.response.EmployeeDetailResponse;
import com.cyclosa.employee.service.EmployeeService;
import com.cyclosa.workflow.dto.request.StartWorkflowRequest;
import com.cyclosa.workflow.dto.response.WorkflowInstanceResponse;
import com.cyclosa.workflow.enums.ApprovalRequestType;
import com.cyclosa.workflow.enums.ApprovalStatus;
import com.cyclosa.workflow.service.WorkflowEngineService;
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
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceExplanationService {

    private final AttendanceExplanationRepository explanationRepository;
    private final AttendanceRecordRepository recordRepository;
    private final ShiftService shiftService;
    private final ShiftAssignmentService assignmentService;
    private final TimesheetService timesheetService;
    private final EmployeeService employeeService;
    private final WorkflowEngineService workflowEngineService;
    private final SecurityPermissionEvaluator permEvaluator;
    private final AttendanceExplanationMapper explanationMapper;
    private final com.cyclosa.attendance.mapper.AttendanceRecordMapper recordMapper;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("workDate", "createdAt", "status");

    @Transactional
    public AttendanceExplanationResponse createExplanation(UUID currentUserId, CreateExplanationRequest req) {
        UUID employeeId = employeeService.findEmployeeIdByUserId(currentUserId)
                .orElseThrow(() -> new AppException(AttendanceErrorCode.CURRENT_USER_NOT_EMPLOYEE));

        EmployeeDetailResponse emp = employeeService.getEmployeeByIdInternal(employeeId);
        UUID companyId = emp.getCompany() != null ? emp.getCompany().getId() : null;
        if (companyId == null) {
            throw AppException.badRequest("Không tìm thấy thông tin công ty của nhân sự");
        }

        if (req.getProposedCheckIn() != null && req.getProposedCheckOut() != null
                && !req.getProposedCheckOut().isAfter(req.getProposedCheckIn())) {
            throw AppException.badRequest("Giờ đề xuất check-out phải sau giờ đề xuất check-in");
        }

        if (explanationRepository.existsByEmployeeIdAndWorkDateAndStatus(employeeId, req.getWorkDate(), ExplanationStatus.PENDING)) {
            throw AppException.badRequest("Đã có đơn giải trình đang chờ xét duyệt cho ngày làm việc này");
        }

        AttendanceExplanation explanation = AttendanceExplanation.builder()
                .companyId(companyId)
                .employeeId(employeeId)
                .attendanceRecordId(req.getAttendanceRecordId())
                .workDate(req.getWorkDate())
                .reasonType(req.getReasonType())
                .proposedCheckIn(req.getProposedCheckIn())
                .proposedCheckOut(req.getProposedCheckOut())
                .reason(req.getReason())
                .proofUrl(req.getProofUrl())
                .status(ExplanationStatus.PENDING)
                .build();

        explanation = explanationRepository.save(explanation);

        // Khởi tạo luồng duyệt qua WorkflowEngineService
        try {
            StartWorkflowRequest wfReq = StartWorkflowRequest.builder()
                    .companyId(companyId)
                    .requestType(ApprovalRequestType.ATTENDANCE_CORRECTION)
                    .requestId(explanation.getId())
                    .requesterEmployeeId(employeeId)
                    .contextVariables(Map.of(
                            "reasonType", req.getReasonType().name(),
                            "workDate", req.getWorkDate().toString()
                    ))
                    .build();

            WorkflowInstanceResponse instance = workflowEngineService.startWorkflow(wfReq);
            explanation.setWorkflowInstanceId(instance.getId());
            explanation = explanationRepository.save(explanation);
            log.info("Started workflow instance id={} for AttendanceExplanation id={}", instance.getId(), explanation.getId());
        } catch (Exception ex) {
            log.warn("Chưa thể kích hoạt workflow tự động cho đơn giải trình: {}", ex.getMessage());
        }

        AttendanceExplanationResponse res = explanationMapper.toResponse(explanation);
        res.setEmployee(employeeService.getEmployeeSummary(employeeId));
        return res;
    }

    @Transactional(readOnly = true)
    public PageData<AttendanceExplanationResponse> getMyExplanations(UUID currentUserId, ExplanationFilter filter, Pageable pageable) {
        UUID employeeId = employeeService.findEmployeeIdByUserId(currentUserId)
                .orElseThrow(() -> new AppException(AttendanceErrorCode.CURRENT_USER_NOT_EMPLOYEE));

        if (filter == null) {
            filter = new ExplanationFilter();
        }
        filter.setExactEmployeeId(employeeId);

        return searchExplanations(filter, pageable);
    }

    @Transactional(readOnly = true)
    public PageData<AttendanceExplanationResponse> getExplanations(UUID companyId, ExplanationFilter filter, Pageable pageable) {
        if (filter == null) {
            filter = new ExplanationFilter();
        }
        UUID effectiveCompanyId = filter.getCompanyId() != null ? filter.getCompanyId() : companyId;
        if (effectiveCompanyId == null) {
            throw AppException.badRequest("Yêu cầu cung cấp ID công ty");
        }
        filter.setCompanyId(effectiveCompanyId);

        applyDataScope(filter);

        return searchExplanations(filter, pageable);
    }

    @Transactional(readOnly = true)
    public com.cyclosa.attendance.dto.response.AttendanceExplanationDetailResponse getExplanationById(UUID companyId, UUID id) {
        AttendanceExplanation explanation = explanationRepository.findById(id)
                .orElseThrow(() -> new AppException(AttendanceErrorCode.EXPLANATION_NOT_FOUND));

        if (companyId != null && !explanation.getCompanyId().equals(companyId)) {
            throw new AppException(AttendanceErrorCode.EXPLANATION_NOT_FOUND);
        }

        com.cyclosa.attendance.dto.response.AttendanceExplanationDetailResponse res = explanationMapper.toDetailResponse(explanation);
        res.setEmployee(employeeService.getEmployeeSummary(explanation.getEmployeeId()));
        if (explanation.getAttendanceRecordId() != null) {
            recordRepository.findById(explanation.getAttendanceRecordId())
                    .ifPresent(r -> res.setAttendanceRecord(recordMapper.toResponse(r)));
        }
        if (explanation.getWorkflowInstanceId() != null) {
            try {
                res.setWorkflowHistory(workflowEngineService.getInstanceHistory(explanation.getWorkflowInstanceId()));
            } catch (Exception ex) {
                log.debug("Workflow history not loaded: {}", ex.getMessage());
            }
        }
        return res;
    }

    @Transactional
    public void handleWorkflowCompleted(UUID explanationId, ApprovalStatus finalStatus) {
        AttendanceExplanation explanation = explanationRepository.findById(explanationId).orElse(null);
        if (explanation == null) {
            log.warn("Không tìm thấy AttendanceExplanation id={}", explanationId);
            return;
        }

        if (finalStatus == ApprovalStatus.APPROVED) {
            explanation.setStatus(ExplanationStatus.APPROVED);

            UUID companyId = explanation.getCompanyId();
            UUID employeeId = explanation.getEmployeeId();
            LocalDate workDate = explanation.getWorkDate();

            AttendanceRecord record = recordRepository
                    .findByCompanyIdAndEmployeeIdAndWorkDate(companyId, employeeId, workDate)
                    .orElseGet(() -> AttendanceRecord.builder()
                            .companyId(companyId)
                            .employeeId(employeeId)
                            .workDate(workDate)
                            .checkInMethod(CheckMethod.MANUAL_ADMIN)
                            .build());

            if (explanation.getProposedCheckIn() != null) {
                record.setCheckInTime(workDate.atTime(explanation.getProposedCheckIn()));
            }
            if (explanation.getProposedCheckOut() != null) {
                record.setCheckOutTime(workDate.atTime(explanation.getProposedCheckOut()));
            }

            // Lấy ca làm việc của ngày để tính lại công
            Shift shift = null;
            if (record.getShiftId() != null) {
                shift = shiftService.findShiftEntity(companyId, record.getShiftId());
            } else {
                Optional<ShiftAssignment> saOpt = assignmentService.findAssignment(companyId, employeeId, workDate);
                if (saOpt.isPresent()) {
                    shift = saOpt.get().getShift();
                } else {
                    shift = shiftService.getDefaultShift(companyId);
                }
            }

            LocalTime startTime = shift != null ? shift.getStartTime() : LocalTime.of(8, 0);
            Integer graceLate = shift != null ? shift.getGraceLateMinutes() : 15;
            LocalTime endTime = shift != null ? shift.getEndTime() : LocalTime.of(17, 30);
            Integer graceEarly = shift != null ? shift.getGraceEarlyMinutes() : 0;
            LocalTime breakStart = shift != null ? shift.getBreakStartTime() : null;
            LocalTime breakEnd = shift != null ? shift.getBreakEndTime() : null;
            BigDecimal stdUnits = shift != null ? shift.getWorkUnits() : BigDecimal.ONE;

            int late = (record.getCheckInTime() != null)
                    ? WorkTimeCalculator.calculateLateMinutes(record.getCheckInTime().toLocalTime(), startTime, graceLate)
                    : 0;
            int early = (record.getCheckOutTime() != null)
                    ? WorkTimeCalculator.calculateEarlyMinutes(record.getCheckOutTime().toLocalTime(), endTime, graceEarly)
                    : 0;

            BigDecimal hours = (record.getCheckInTime() != null && record.getCheckOutTime() != null)
                    ? WorkTimeCalculator.calculateActualHours(record.getCheckInTime(), record.getCheckOutTime(), breakStart, breakEnd)
                    : BigDecimal.ZERO;
            BigDecimal units = WorkTimeCalculator.calculateActualWorkUnits(hours, stdUnits);

            record.setLateMinutes(late);
            record.setEarlyMinutes(early);
            record.setActualHours(hours);
            record.setActualWorkUnits(units);
            record.setStatus(AttendanceStatus.EXPLAINED);
            recordRepository.save(record);
            log.info("Updated AttendanceRecord after approved explanation id={}", explanationId);

            // Đồng bộ lại bảng công tháng
            timesheetService.recalculateTimesheetForEmployee(companyId, employeeId, workDate.getMonthValue(), workDate.getYear());
        } else if (finalStatus == ApprovalStatus.REJECTED) {
            explanation.setStatus(ExplanationStatus.REJECTED);
        }

        explanationRepository.save(explanation);
    }

    private PageData<AttendanceExplanationResponse> searchExplanations(ExplanationFilter filter, Pageable pageable) {
        final ExplanationFilter f = filter;
        Specification<AttendanceExplanation> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (f.getCompanyId() != null) {
                predicates.add(cb.equal(root.get("companyId"), f.getCompanyId()));
            }

            if (f.getExactEmployeeId() != null) {
                predicates.add(cb.equal(root.get("employeeId"), f.getExactEmployeeId()));
            } else if (f.getAllowedEmployeeIds() != null) {
                predicates.add(root.get("employeeId").in(f.getAllowedEmployeeIds()));
            } else if (f.getEmployeeId() != null) {
                predicates.add(cb.equal(root.get("employeeId"), f.getEmployeeId()));
            }

            if (f.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), f.getStatus()));
            }
            if (f.getReasonType() != null) {
                predicates.add(cb.equal(root.get("reasonType"), f.getReasonType()));
            }
            if (f.getFromDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("workDate"), f.getFromDate()));
            }
            if (f.getToDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("workDate"), f.getToDate()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Pageable effectivePageable = (pageable != null && pageable.isPaged()) ? pageable : filter.toPageable("createdAt", ALLOWED_SORT_FIELDS);
        Page<AttendanceExplanation> page = explanationRepository.findAll(spec, effectivePageable);
        if (page.isEmpty()) {
            return PageData.of(page, List.of());
        }

        Set<UUID> empIds = page.getContent().stream().map(AttendanceExplanation::getEmployeeId).collect(Collectors.toSet());
        Map<UUID, EmployeeSummary> empMap = employeeService.getEmployeeSummaries(empIds);

        List<AttendanceExplanationResponse> list = page.getContent().stream().map(e -> {
            AttendanceExplanationResponse res = explanationMapper.toResponse(e);
            res.setEmployee(empMap.get(e.getEmployeeId()));
            return res;
        }).toList();

        return PageData.of(page, list);
    }

    private void applyDataScope(ExplanationFilter filter) {
        DataScope scope = permEvaluator.getDataScope("attendance.explain.view").orElse(DataScope.OWN);
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
