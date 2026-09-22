package com.cyclosa.leave.service;

import com.cyclosa.attendance.entity.AttendanceRecord;
import com.cyclosa.attendance.repository.AttendanceRecordRepository;
import com.cyclosa.common.dto.summary.CompanySummary;
import com.cyclosa.common.dto.summary.EmployeeSummary;
import com.cyclosa.common.enums.DataScope;
import com.cyclosa.common.exception.AppException;
import com.cyclosa.common.response.PageData;
import com.cyclosa.common.security.SecurityPermissionEvaluator;
import com.cyclosa.common.util.PageableUtils;
import com.cyclosa.common.util.SecurityUtils;
import com.cyclosa.employee.dto.response.EmployeeDetailResponse;
import com.cyclosa.employee.service.EmployeeService;
import com.cyclosa.leave.dto.request.CreateLeaveRequestRequest;
import com.cyclosa.leave.dto.request.LeaveRequestFilter;
import com.cyclosa.leave.dto.request.RejectLeaveRequest;
import com.cyclosa.leave.dto.response.LeaveRequestDetailResponse;
import com.cyclosa.leave.dto.response.LeaveRequestResponse;
import com.cyclosa.leave.dto.response.LeaveTypeResponse;
import com.cyclosa.leave.entity.LeaveBalance;
import com.cyclosa.leave.entity.LeaveRequest;
import com.cyclosa.leave.entity.LeaveType;
import com.cyclosa.leave.enums.LeaveCategory;
import com.cyclosa.leave.enums.LeaveRequestStatus;
import com.cyclosa.leave.enums.LeaveSession;
import com.cyclosa.leave.exception.LeaveErrorCode;
import com.cyclosa.leave.mapper.LeaveRequestMapper;
import com.cyclosa.leave.repository.LeaveBalanceRepository;
import com.cyclosa.leave.repository.LeaveRequestRepository;
import com.cyclosa.leave.repository.LeaveTypeRepository;
import com.cyclosa.organization.service.CompanyService;
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
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeaveRequestService {

    private final LeaveRequestRepository requestRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final LeaveBalanceRepository balanceRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final LeaveBalanceService balanceService;
    private final LeaveTypeService leaveTypeService;
    private final LeaveCalculationEngine calculationEngine;
    private final EmployeeService employeeService;
    private final CompanyService companyService;
    private final WorkflowEngineService workflowEngineService;
    private final SecurityPermissionEvaluator permEvaluator;
    private final LeaveRequestMapper requestMapper;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("startDate", "endDate", "totalDays", "status", "createdAt");

    @Transactional
    public LeaveRequestResponse createLeaveRequest(UUID currentUserId, CreateLeaveRequestRequest req) {
        if (req.getStartDate().isAfter(req.getEndDate())) {
            throw new AppException(LeaveErrorCode.INVALID_DATE_RANGE);
        }

        boolean isHrManager = permEvaluator.has("leave.manage");

        // Xác định nhân viên xin nghỉ
        UUID targetEmployeeId = (req.getEmployeeId() != null && isHrManager)
                ? req.getEmployeeId()
                : employeeService.findEmployeeIdByUserId(currentUserId)
                .orElseThrow(() -> new AppException(LeaveErrorCode.CURRENT_USER_NOT_EMPLOYEE));

        EmployeeDetailResponse empDetail = employeeService.getEmployeeByIdInternal(targetEmployeeId);
        UUID companyId = empDetail.getCompany() != null ? empDetail.getCompany().getId() : null;
        if (companyId == null) {
            throw AppException.badRequest("Nhân viên chưa thuộc công ty nào");
        }

        // Validate ngày bắt đầu không được ở quá khứ (trừ HR thao tác)
        if (!isHrManager && req.getStartDate().isBefore(LocalDate.now())) {
            throw new AppException(LeaveErrorCode.LEAVE_DATE_IN_PAST);
        }

        LeaveType leaveType = leaveTypeRepository.findById(req.getLeaveTypeId())
                .orElseThrow(() -> new AppException(LeaveErrorCode.LEAVE_TYPE_NOT_FOUND));

        if (!Boolean.TRUE.equals(leaveType.getIsActive())) {
            throw new AppException(LeaveErrorCode.LEAVE_TYPE_NOT_FOUND, "Loại nghỉ phép này đã ngừng áp dụng");
        }

        // 1. Tính số ngày công thực trừ
        LeaveSession session = req.getSession() != null ? req.getSession() : LeaveSession.FULL_DAY;
        BigDecimal totalDays = calculationEngine.calculateLeaveDays(leaveType, req.getStartDate(), req.getEndDate(), session);

        if (totalDays.compareTo(BigDecimal.ZERO) <= 0) {
            throw AppException.badRequest("Khoảng thời gian đã chọn không có ngày làm việc nào cần xin nghỉ");
        }

        // 2. Chặn trùng lặp khoảng thời gian
        List<LeaveRequestStatus> activeStatuses = List.of(LeaveRequestStatus.PENDING_APPROVAL, LeaveRequestStatus.APPROVED);
        if (requestRepository.hasOverlappingRequest(targetEmployeeId, req.getStartDate(), req.getEndDate(), activeStatuses, null)) {
            throw new AppException(LeaveErrorCode.OVERLAPPING_LEAVE_REQUEST);
        }

        // 3. Chặn xin nghỉ vào ngày đã có check-in chấm công đi làm
        List<AttendanceRecord> attendedDays = attendanceRecordRepository
                .findAllByEmployeeIdAndWorkDateBetweenOrderByWorkDateAsc(targetEmployeeId, req.getStartDate(), req.getEndDate());
        for (AttendanceRecord r : attendedDays) {
            if (r.getCheckInTime() != null) {
                throw new AppException(LeaveErrorCode.CANNOT_REQUEST_LEAVE_ON_WORKED_DAY,
                        String.format("Ngày %s nhân viên đã thực hiện chấm công đi làm, không thể gửi đơn nghỉ phép", r.getWorkDate()));
            }
        }

        // 4. Kiểm tra số dư phép nếu là ANNUAL hoặc SICK
        int leaveYear = req.getStartDate().getYear();
        if (leaveType.getCategory() == LeaveCategory.ANNUAL || leaveType.getCategory() == LeaveCategory.SICK) {
            LeaveBalance balance = balanceRepository
                    .findByCompanyIdAndEmployeeIdAndLeaveTypeIdAndYear(companyId, targetEmployeeId, leaveType.getId(), leaveYear)
                    .orElse(null);

            if (balance == null || balance.getAvailableDays().compareTo(totalDays) < 0) {
                BigDecimal available = balance != null ? balance.getAvailableDays() : BigDecimal.ZERO;
                throw new AppException(LeaveErrorCode.INSUFFICIENT_LEAVE_BALANCE,
                        String.format("Số dư phép khả dụng không đủ (Hiện có: %.1f ngày, Cần xin: %.1f ngày)",
                                available.doubleValue(), totalDays.doubleValue()));
            }
        }

        // 5. Tạo thực thể LeaveRequest
        LeaveRequest entity = requestMapper.toEntity(req);
        entity.setCompanyId(companyId);
        entity.setEmployeeId(targetEmployeeId);
        entity.setSession(session);
        entity.setTotalDays(totalDays);

        // Nếu loại phép không yêu cầu duyệt -> Auto-approve
        if (!Boolean.TRUE.equals(leaveType.getRequiresApproval())) {
            entity.setStatus(LeaveRequestStatus.APPROVED);
            entity.setApprovedAt(LocalDateTime.now());
            entity = requestRepository.save(entity);

            // Cập nhật số dư trực tiếp
            balanceService.commitApprovedLeave(entity);
            log.info("Auto-approved LeaveRequest id={}, employeeId={}, totalDays={}", entity.getId(), targetEmployeeId, totalDays);
        } else {
            entity.setStatus(LeaveRequestStatus.PENDING_APPROVAL);
            entity = requestRepository.save(entity);

            // Ghi nhận số ngày pending
            balanceService.recordPendingLeave(companyId, targetEmployeeId, leaveType.getId(), leaveYear, totalDays);

            // Khởi chạy Workflow Engine
            try {
                StartWorkflowRequest wfReq = StartWorkflowRequest.builder()
                        .companyId(companyId)
                        .requestType(ApprovalRequestType.LEAVE_REQUEST)
                        .requestId(entity.getId())
                        .requesterEmployeeId(targetEmployeeId)
                        .contextVariables(Map.of(
                                "totalDays", totalDays.toString(),
                                "startDate", req.getStartDate().toString(),
                                "endDate", req.getEndDate().toString(),
                                "leaveCategory", leaveType.getCategory().name(),
                                "leaveTypeCode", leaveType.getCode()
                        ))
                        .build();

                WorkflowInstanceResponse instance = workflowEngineService.startWorkflow(wfReq);
                entity.setWorkflowInstanceId(instance.getId());
                entity = requestRepository.save(entity);
                log.info("Started workflow instance id={} for LeaveRequest id={}", instance.getId(), entity.getId());
            } catch (Exception ex) {
                log.error("Lỗi khi khởi chạy workflow cho LeaveRequest id={}: {}", entity.getId(), ex.getMessage());
            }
        }

        return toPopulatedResponse(entity);
    }

    @Transactional(readOnly = true)
    public PageData<LeaveRequestResponse> getMyLeaveRequests(LeaveRequestFilter filter, Pageable pageable) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        UUID employeeId = employeeService.findEmployeeIdByUserId(currentUserId)
                .orElseThrow(() -> new AppException(LeaveErrorCode.CURRENT_USER_NOT_EMPLOYEE));

        if (filter == null) {
            filter = new LeaveRequestFilter();
        }
        filter.setExactEmployeeId(employeeId);
        return searchRequests(filter, pageable);
    }

    @Transactional(readOnly = true)
    public PageData<LeaveRequestResponse> getLeaveRequests(UUID companyId, LeaveRequestFilter filter, Pageable pageable) {
        if (filter == null) {
            filter = new LeaveRequestFilter();
        }
        UUID effectiveCompanyId = filter.getCompanyId() != null ? filter.getCompanyId() : companyId;
        if (effectiveCompanyId == null) {
            throw AppException.badRequest("Yêu cầu cung cấp ID công ty");
        }
        filter.setCompanyId(effectiveCompanyId);

        applyDataScope(filter);
        return searchRequests(filter, pageable);
    }

    @Transactional(readOnly = true)
    public LeaveRequestDetailResponse getLeaveRequestById(UUID companyId, UUID id) {
        LeaveRequest entity = requestRepository.findById(id)
                .orElseThrow(() -> new AppException(LeaveErrorCode.LEAVE_REQUEST_NOT_FOUND));

        if (companyId != null && !entity.getCompanyId().equals(companyId)) {
            throw new AppException(LeaveErrorCode.LEAVE_REQUEST_NOT_FOUND);
        }

        LeaveRequestDetailResponse res = requestMapper.toDetailResponse(entity);
        res.setEmployee(employeeService.getEmployeeSummary(entity.getEmployeeId()));
        if (entity.getCompanyId() != null) {
            res.setCompany(companyService.getCompanySummary(entity.getCompanyId()));
        }
        try {
            res.setLeaveType(leaveTypeService.getLeaveTypeById(entity.getCompanyId(), entity.getLeaveTypeId()));
        } catch (Exception ignored) {}

        if (entity.getWorkflowInstanceId() != null) {
            try {
                res.setWorkflowHistory(workflowEngineService.getInstanceHistory(entity.getWorkflowInstanceId()));
            } catch (Exception ex) {
                log.debug("Workflow history not loaded: {}", ex.getMessage());
            }
        }
        return res;
    }

    @Transactional
    public LeaveRequestResponse approveLeaveRequest(UUID companyId, UUID id) {
        LeaveRequest entity = requestRepository.findById(id)
                .orElseThrow(() -> new AppException(LeaveErrorCode.LEAVE_REQUEST_NOT_FOUND));

        if (companyId != null && !entity.getCompanyId().equals(companyId)) {
            throw new AppException(LeaveErrorCode.LEAVE_REQUEST_NOT_FOUND);
        }

        if (entity.getStatus() != LeaveRequestStatus.PENDING_APPROVAL) {
            throw new AppException(LeaveErrorCode.LEAVE_REQUEST_ALREADY_PROCESSED);
        }

        entity.setStatus(LeaveRequestStatus.APPROVED);
        entity.setApprovedAt(LocalDateTime.now());
        entity = requestRepository.save(entity);

        balanceService.commitApprovedLeave(entity);
        log.info("Approved LeaveRequest id={}", id);
        return toPopulatedResponse(entity);
    }

    @Transactional
    public LeaveRequestResponse rejectLeaveRequest(UUID companyId, UUID id, RejectLeaveRequest rejectReq) {
        LeaveRequest entity = requestRepository.findById(id)
                .orElseThrow(() -> new AppException(LeaveErrorCode.LEAVE_REQUEST_NOT_FOUND));

        if (companyId != null && !entity.getCompanyId().equals(companyId)) {
            throw new AppException(LeaveErrorCode.LEAVE_REQUEST_NOT_FOUND);
        }

        if (entity.getStatus() != LeaveRequestStatus.PENDING_APPROVAL) {
            throw new AppException(LeaveErrorCode.LEAVE_REQUEST_ALREADY_PROCESSED);
        }

        entity.setStatus(LeaveRequestStatus.REJECTED);
        entity.setRejectionReason(rejectReq.getReason());
        entity = requestRepository.save(entity);

        balanceService.rollbackLeave(entity, false);
        log.info("Rejected LeaveRequest id={}, reason={}", id, rejectReq.getReason());
        return toPopulatedResponse(entity);
    }

    @Transactional
    public LeaveRequestResponse cancelLeaveRequest(UUID currentUserId, UUID id) {
        LeaveRequest entity = requestRepository.findById(id)
                .orElseThrow(() -> new AppException(LeaveErrorCode.LEAVE_REQUEST_NOT_FOUND));

        boolean isHr = permEvaluator.has("leave.manage");
        UUID currentEmpId = employeeService.findEmployeeIdByUserId(currentUserId).orElse(null);

        if (!isHr && (currentEmpId == null || !currentEmpId.equals(entity.getEmployeeId()))) {
            throw AppException.forbidden("Bạn không có quyền hủy đơn nghỉ phép của người khác");
        }

        if (entity.getStatus() != LeaveRequestStatus.PENDING_APPROVAL && entity.getStatus() != LeaveRequestStatus.APPROVED) {
            throw new AppException(LeaveErrorCode.LEAVE_REQUEST_ALREADY_PROCESSED);
        }

        // Chặn hủy nếu ngày nghỉ đã đến hoặc qua
        if (entity.getStartDate().isBefore(LocalDate.now()) || entity.getStartDate().isEqual(LocalDate.now())) {
            throw new AppException(LeaveErrorCode.LEAVE_ALREADY_STARTED);
        }

        boolean wasApproved = entity.getStatus() == LeaveRequestStatus.APPROVED;
        entity.setStatus(LeaveRequestStatus.CANCELLED);
        entity.setCancelledAt(LocalDateTime.now());
        entity.setCancelledBy(currentEmpId);
        entity = requestRepository.save(entity);

        balanceService.rollbackLeave(entity, wasApproved);
        log.info("Cancelled LeaveRequest id={}", id);
        return toPopulatedResponse(entity);
    }

    /**
     * Nhận callback sự kiện kết thúc luồng Workflow duyệt.
     */
    @Transactional
    public void handleWorkflowCompleted(UUID requestId, ApprovalStatus finalStatus, String reason) {
        LeaveRequest entity = requestRepository.findById(requestId).orElse(null);
        if (entity == null || entity.getStatus() != LeaveRequestStatus.PENDING_APPROVAL) {
            return;
        }

        if (finalStatus == ApprovalStatus.APPROVED) {
            entity.setStatus(LeaveRequestStatus.APPROVED);
            entity.setApprovedAt(LocalDateTime.now());
            requestRepository.save(entity);
            balanceService.commitApprovedLeave(entity);
            log.info("Workflow approved LeaveRequest id={}", requestId);
        } else if (finalStatus == ApprovalStatus.REJECTED) {
            entity.setStatus(LeaveRequestStatus.REJECTED);
            entity.setRejectionReason(reason);
            requestRepository.save(entity);
            balanceService.rollbackLeave(entity, false);
            log.info("Workflow rejected LeaveRequest id={}, reason={}", requestId, reason);
        } else if (finalStatus == ApprovalStatus.CANCELLED) {
            entity.setStatus(LeaveRequestStatus.CANCELLED);
            entity.setCancelledAt(LocalDateTime.now());
            requestRepository.save(entity);
            balanceService.rollbackLeave(entity, false);
            log.info("Workflow cancelled LeaveRequest id={}", requestId);
        }
    }

    // --- Public Service Contracts cho Module 06 (Attendance/Timesheet) & Module 08 (Payroll) ---

    @Transactional(readOnly = true)
    public BigDecimal getPaidLeaveDays(UUID employeeId, LocalDate fromDate, LocalDate toDate) {
        List<LeaveRequest> requests = requestRepository.findApprovedRequestsInRange(employeeId, fromDate, toDate);
        BigDecimal total = BigDecimal.ZERO;
        for (LeaveRequest r : requests) {
            LeaveType type = leaveTypeRepository.findById(r.getLeaveTypeId()).orElse(null);
            if (type != null && Boolean.TRUE.equals(type.getIsPaid())) {
                total = total.add(r.getTotalDays());
            }
        }
        return total;
    }

    @Transactional(readOnly = true)
    public BigDecimal getUnpaidLeaveDays(UUID employeeId, LocalDate fromDate, LocalDate toDate) {
        List<LeaveRequest> requests = requestRepository.findApprovedRequestsInRange(employeeId, fromDate, toDate);
        BigDecimal total = BigDecimal.ZERO;
        for (LeaveRequest r : requests) {
            LeaveType type = leaveTypeRepository.findById(r.getLeaveTypeId()).orElse(null);
            if (type != null && !Boolean.TRUE.equals(type.getIsPaid())) {
                total = total.add(r.getTotalDays());
            }
        }
        return total;
    }

    @Transactional(readOnly = true)
    public Map<UUID, BigDecimal> getMonthlyPaidLeaveDaysBatch(UUID companyId, int month, int year, Set<UUID> employeeIds) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        List<LeaveRequest> requests = requestRepository.findApprovedRequestsByCompanyInRange(companyId, start, end);
        Map<UUID, BigDecimal> result = new HashMap<>();

        for (LeaveRequest r : requests) {
            if (employeeIds != null && !employeeIds.isEmpty() && !employeeIds.contains(r.getEmployeeId())) {
                continue;
            }
            LeaveType type = leaveTypeRepository.findById(r.getLeaveTypeId()).orElse(null);
            if (type != null && Boolean.TRUE.equals(type.getIsPaid())) {
                result.merge(r.getEmployeeId(), r.getTotalDays(), BigDecimal::add);
            }
        }
        return result;
    }

    private PageData<LeaveRequestResponse> searchRequests(LeaveRequestFilter filter, Pageable pageable) {
        final LeaveRequestFilter f = filter;
        Specification<LeaveRequest> spec = (root, query, cb) -> {
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

            if (f.getLeaveTypeId() != null) {
                predicates.add(cb.equal(root.get("leaveTypeId"), f.getLeaveTypeId()));
            }
            if (f.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), f.getStatus()));
            }
            if (f.getFromDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("startDate"), f.getFromDate()));
            }
            if (f.getToDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("endDate"), f.getToDate()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Pageable effectivePageable = (pageable != null && pageable.isPaged()) ? pageable : filter.toPageable("createdAt", ALLOWED_SORT_FIELDS);
        Page<LeaveRequest> page = requestRepository.findAll(spec, effectivePageable);

        Set<UUID> empIds = page.getContent().stream().map(LeaveRequest::getEmployeeId).collect(Collectors.toSet());
        Map<UUID, EmployeeSummary> empMap = employeeService.getEmployeeSummaries(empIds);

        Set<UUID> typeIds = page.getContent().stream().map(LeaveRequest::getLeaveTypeId).collect(Collectors.toSet());
        Map<UUID, LeaveTypeResponse> typeMap = new HashMap<>();
        for (UUID tId : typeIds) {
            try {
                typeMap.put(tId, leaveTypeService.getLeaveTypeById(f.getCompanyId(), tId));
            } catch (Exception ignored) {}
        }

        List<LeaveRequestResponse> list = page.getContent().stream().map(r -> {
            LeaveRequestResponse res = requestMapper.toResponse(r);
            res.setEmployee(empMap.get(r.getEmployeeId()));
            res.setLeaveType(typeMap.get(r.getLeaveTypeId()));
            return res;
        }).toList();

        return PageData.of(page, list);
    }

    private LeaveRequestResponse toPopulatedResponse(LeaveRequest entity) {
        LeaveRequestResponse res = requestMapper.toResponse(entity);
        res.setEmployee(employeeService.getEmployeeSummary(entity.getEmployeeId()));
        try {
            res.setLeaveType(leaveTypeService.getLeaveTypeById(entity.getCompanyId(), entity.getLeaveTypeId()));
        } catch (Exception ignored) {}
        return res;
    }

    private void applyDataScope(LeaveRequestFilter filter) {
        DataScope scope = permEvaluator.getDataScope("leave.view").orElse(DataScope.OWN);
        Optional<UUID> currentUserIdOpt = SecurityUtils.getCurrentUserIdOptional();

        if (scope == DataScope.ALL || scope == DataScope.COMPANY) {
            return;
        }

        if (currentUserIdOpt.isEmpty()) {
            filter.setExactEmployeeId(UUID.randomUUID());
            return;
        }

        UUID currentUserId = currentUserIdOpt.get();
        Optional<UUID> currentEmpIdOpt = employeeService.findEmployeeIdByUserId(currentUserId);
        if (currentEmpIdOpt.isEmpty()) {
            filter.setExactEmployeeId(UUID.randomUUID());
            return;
        }

        UUID currentEmpId = currentEmpIdOpt.get();
        switch (scope) {
            case OWN -> filter.setExactEmployeeId(currentEmpId);
            case DEPARTMENT -> {
                EmployeeDetailResponse empDetail = employeeService.getEmployeeByIdInternal(currentEmpId);
                UUID unitId = (empDetail.getOrganizationalUnit() != null)
                        ? empDetail.getOrganizationalUnit().getId()
                        : null;
                if (unitId != null && filter.getCompanyId() != null) {
                    Set<UUID> deptEmpIds = employeeService.getEmployeeIdsByDepartment(filter.getCompanyId(), unitId);
                    filter.setAllowedEmployeeIds(deptEmpIds.isEmpty() ? Set.of(currentEmpId) : deptEmpIds);
                } else {
                    filter.setExactEmployeeId(currentEmpId);
                }
            }
            case TEAM -> {
                Set<UUID> subIds = employeeService.getSubordinateEmployeeIds(currentEmpId);
                Set<UUID> allowed = new HashSet<>(subIds);
                allowed.add(currentEmpId);
                filter.setAllowedEmployeeIds(allowed);
            }
            default -> filter.setExactEmployeeId(currentEmpId);
        }
    }
}
