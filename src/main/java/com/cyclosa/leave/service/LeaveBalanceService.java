package com.cyclosa.leave.service;

import com.cyclosa.common.dto.summary.EmployeeSummary;
import com.cyclosa.common.enums.DataScope;
import com.cyclosa.common.exception.AppException;
import com.cyclosa.common.response.PageData;
import com.cyclosa.common.security.SecurityPermissionEvaluator;
import com.cyclosa.common.util.SecurityUtils;
import com.cyclosa.contract.dto.response.ContractResponse;
import com.cyclosa.contract.service.ContractService;
import com.cyclosa.employee.dto.response.EmployeeDetailResponse;
import com.cyclosa.employee.service.EmployeeService;
import com.cyclosa.leave.dto.request.LeaveBalanceFilter;
import com.cyclosa.leave.dto.request.RecalculateLeaveBalanceRequest;
import com.cyclosa.leave.dto.response.LeaveBalanceResponse;
import com.cyclosa.leave.dto.response.LeaveTypeResponse;
import com.cyclosa.leave.dto.response.UnusedLeavePayoutResponse;
import com.cyclosa.leave.entity.LeaveBalance;
import com.cyclosa.leave.entity.LeavePolicy;
import com.cyclosa.leave.entity.LeaveRequest;
import com.cyclosa.leave.entity.LeaveType;
import com.cyclosa.leave.enums.JobConditionLevel;
import com.cyclosa.leave.enums.LeaveCategory;
import com.cyclosa.leave.exception.LeaveErrorCode;
import com.cyclosa.leave.mapper.LeaveBalanceMapper;
import com.cyclosa.leave.repository.LeaveBalanceRepository;
import com.cyclosa.leave.repository.LeaveTypeRepository;
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
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeaveBalanceService {

    private final LeaveBalanceRepository balanceRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final LeavePolicyService leavePolicyService;
    private final LeaveCalculationEngine calculationEngine;
    private final EmployeeService employeeService;
    private final com.cyclosa.employee.repository.EmployeeRepository employeeRepository;
    private final ContractService contractService;
    private final LeaveTypeService leaveTypeService;
    private final SecurityPermissionEvaluator permEvaluator;
    private final LeaveBalanceMapper balanceMapper;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("year", "totalDays", "usedDays", "createdAt");

    @Transactional(readOnly = true)
    public List<LeaveBalanceResponse> getMyBalances(Integer year) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        UUID employeeId = employeeService.findEmployeeIdByUserId(currentUserId)
                .orElseThrow(() -> new AppException(LeaveErrorCode.CURRENT_USER_NOT_EMPLOYEE));

        int targetYear = year != null ? year : LocalDate.now().getYear();
        List<LeaveBalance> balances = balanceRepository.findAllByEmployeeIdAndYear(employeeId, targetYear);

        EmployeeSummary empSummary = employeeService.getEmployeeSummary(employeeId);
        return balances.stream().map(b -> {
            LeaveBalanceResponse res = balanceMapper.toResponse(b);
            res.setEmployee(empSummary);
            try {
                res.setLeaveType(leaveTypeService.getLeaveTypeById(b.getCompanyId(), b.getLeaveTypeId()));
            } catch (Exception ignored) {}
            return res;
        }).toList();
    }

    @Transactional(readOnly = true)
    public PageData<LeaveBalanceResponse> getBalances(UUID companyId, LeaveBalanceFilter filter, Pageable pageable) {
        if (filter == null) {
            filter = new LeaveBalanceFilter();
        }
        UUID effectiveCompanyId = filter.getCompanyId() != null ? filter.getCompanyId() : companyId;
        if (effectiveCompanyId == null) {
            throw AppException.badRequest("Yêu cầu cung cấp ID công ty");
        }
        filter.setCompanyId(effectiveCompanyId);

        applyDataScope(filter);

        final LeaveBalanceFilter f = filter;
        Specification<LeaveBalance> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("companyId"), f.getCompanyId()));

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
            if (f.getYear() != null) {
                predicates.add(cb.equal(root.get("year"), f.getYear()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Pageable effectivePageable = (pageable != null && pageable.isPaged()) ? pageable : filter.toPageable("createdAt", ALLOWED_SORT_FIELDS);
        Page<LeaveBalance> page = balanceRepository.findAll(spec, effectivePageable);

        Set<UUID> empIds = page.getContent().stream().map(LeaveBalance::getEmployeeId).collect(Collectors.toSet());
        Map<UUID, EmployeeSummary> empMap = employeeService.getEmployeeSummaries(empIds);

        Set<UUID> typeIds = page.getContent().stream().map(LeaveBalance::getLeaveTypeId).collect(Collectors.toSet());
        Map<UUID, LeaveTypeResponse> typeMap = new HashMap<>();
        for (UUID tId : typeIds) {
            try {
                typeMap.put(tId, leaveTypeService.getLeaveTypeById(effectiveCompanyId, tId));
            } catch (Exception ignored) {}
        }

        List<LeaveBalanceResponse> list = page.getContent().stream().map(b -> {
            LeaveBalanceResponse res = balanceMapper.toResponse(b);
            res.setEmployee(empMap.get(b.getEmployeeId()));
            res.setLeaveType(typeMap.get(b.getLeaveTypeId()));
            return res;
        }).toList();

        return PageData.of(page, list);
    }

    /**
     * Khởi chạy tính toán lại số dư phép năm cho nhân sự theo chính sách BLLĐ.
     */
    @Transactional
    public int recalculateBalances(UUID companyId, RecalculateLeaveBalanceRequest request) {
        if (companyId == null) {
            throw AppException.badRequest("Yêu cầu cung cấp ID công ty");
        }

        int targetYear = request.getYear() != null ? request.getYear() : LocalDate.now().getYear();

        // Tìm loại ngày nghỉ ANNUAL của công ty
        LeaveType annualType = leaveTypeRepository.findFirstByCategoryAndCompanyId(LeaveCategory.ANNUAL, companyId)
                .or(() -> leaveTypeRepository.findFirstByCategoryAndCompanyIdIsNull(LeaveCategory.ANNUAL))
                .orElseThrow(() -> new AppException(LeaveErrorCode.LEAVE_TYPE_NOT_FOUND, "Chưa thiết lập loại ngày nghỉ phép năm (ANNUAL)"));

        Set<UUID> targetEmployeeIds = request.getEmployeeIds();
        if (targetEmployeeIds == null || targetEmployeeIds.isEmpty()) {
            targetEmployeeIds = employeeRepository.findAll((root, query, cb) -> cb.equal(root.get("companyId"), companyId))
                    .stream()
                    .map(com.cyclosa.employee.entity.Employee::getId)
                    .collect(Collectors.toSet());
        }

        int count = 0;
        for (UUID empId : targetEmployeeIds) {
            recalculateEmployeeAnnualLeave(companyId, empId, annualType.getId(), targetYear);
            count++;
        }

        log.info("Recalculated annual leave balances for companyId={}, year={}, count={}", companyId, targetYear, count);
        return count;
    }

    @Transactional
    public LeaveBalance recalculateEmployeeAnnualLeave(UUID companyId, UUID employeeId, UUID annualTypeId, int targetYear) {
        EmployeeDetailResponse empDetail = employeeService.getEmployeeByIdInternal(employeeId);
        LocalDate hireDate = empDetail.getHireDate() != null ? empDetail.getHireDate() : LocalDate.of(targetYear, 1, 1);

        // Tìm chính sách nghỉ phép của công ty cho loại ANNUAL
        Optional<LeavePolicy> policyOpt = leavePolicyService.findEffectivePolicy(companyId, annualTypeId, JobConditionLevel.NORMAL);
        BigDecimal customBase = policyOpt.map(LeavePolicy::getAccrualDaysPerYear).orElse(BigDecimal.valueOf(12.0));
        int seniorityBonusYears = policyOpt.map(LeavePolicy::getSeniorityBonusEveryYears).orElse(5);
        BigDecimal seniorityBonusDays = policyOpt.map(LeavePolicy::getSeniorityBonusDays).orElse(BigDecimal.valueOf(1.0));

        BigDecimal totalEntitlement = calculationEngine.calculateAnnualLeaveEntitlement(
                hireDate, targetYear, JobConditionLevel.NORMAL, customBase, seniorityBonusYears, seniorityBonusDays
        );

        LeaveBalance balance = balanceRepository
                .findByCompanyIdAndEmployeeIdAndLeaveTypeIdAndYear(companyId, employeeId, annualTypeId, targetYear)
                .orElseGet(() -> LeaveBalance.builder()
                        .companyId(companyId)
                        .employeeId(employeeId)
                        .leaveTypeId(annualTypeId)
                        .year(targetYear)
                        .usedDays(BigDecimal.ZERO)
                        .pendingDays(BigDecimal.ZERO)
                        .carriedOverDays(BigDecimal.ZERO)
                        .build());

        balance.setTotalDays(totalEntitlement);
        return balanceRepository.save(balance);
    }

    /**
     * Tính toán số tiền phép năm chưa sử dụng để chi trả khi thôi việc (Điều 113.3 BLLĐ 2019).
     */
    @Transactional(readOnly = true)
    public UnusedLeavePayoutResponse calculateUnusedAnnualLeavePayout(UUID companyId, UUID employeeId) {
        int currentYear = LocalDate.now().getYear();

        LeaveType annualType = leaveTypeRepository.findFirstByCategoryAndCompanyId(LeaveCategory.ANNUAL, companyId)
                .or(() -> leaveTypeRepository.findFirstByCategoryAndCompanyIdIsNull(LeaveCategory.ANNUAL))
                .orElseThrow(() -> new AppException(LeaveErrorCode.LEAVE_TYPE_NOT_FOUND, "Không tìm thấy loại nghỉ phép năm"));

        LeaveBalance balance = balanceRepository
                .findByCompanyIdAndEmployeeIdAndLeaveTypeIdAndYear(companyId, employeeId, annualType.getId(), currentYear)
                .orElse(null);

        BigDecimal remainingDays = balance != null ? balance.getRemainingDays() : BigDecimal.ZERO;

        Optional<ContractResponse> contractOpt = contractService.getActiveContractByEmployee(employeeId);
        BigDecimal basicSalary = contractOpt.map(ContractResponse::getBasicSalary).orElse(BigDecimal.ZERO);
        BigDecimal standardWorkDays = BigDecimal.valueOf(22); // Tiêu chuẩn 22 ngày làm việc/tháng

        BigDecimal dailyRate = (basicSalary.compareTo(BigDecimal.ZERO) > 0)
                ? basicSalary.divide(standardWorkDays, 4, java.math.RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal totalPayout = calculationEngine.calculateUnusedLeavePayout(basicSalary, standardWorkDays, remainingDays);

        return UnusedLeavePayoutResponse.builder()
                .employeeId(employeeId)
                .employee(employeeService.getEmployeeSummary(employeeId))
                .year(currentYear)
                .remainingAnnualLeaveDays(remainingDays)
                .basicSalary(basicSalary)
                .dailyRate(dailyRate)
                .totalPayoutAmount(totalPayout)
                .build();
    }

    /**
     * Tăng pending_days khi đơn nghỉ phép được tạo và chờ duyệt.
     */
    @Transactional
    public void recordPendingLeave(UUID companyId, UUID employeeId, UUID leaveTypeId, int year, BigDecimal days) {
        LeaveBalance balance = balanceRepository
                .findByCompanyIdAndEmployeeIdAndLeaveTypeIdAndYear(companyId, employeeId, leaveTypeId, year)
                .orElse(null);

        if (balance != null) {
            BigDecimal newPending = (balance.getPendingDays() != null ? balance.getPendingDays() : BigDecimal.ZERO).add(days);
            balance.setPendingDays(newPending);
            balanceRepository.save(balance);
        }
    }

    /**
     * Chuyển pending_days sang used_days khi đơn nghỉ phép được APPROVED.
     */
    @Transactional
    public void commitApprovedLeave(LeaveRequest request) {
        int year = request.getStartDate().getYear();
        LeaveBalance balance = balanceRepository
                .findByCompanyIdAndEmployeeIdAndLeaveTypeIdAndYear(request.getCompanyId(), request.getEmployeeId(), request.getLeaveTypeId(), year)
                .orElse(null);

        if (balance != null) {
            BigDecimal pending = balance.getPendingDays() != null ? balance.getPendingDays() : BigDecimal.ZERO;
            BigDecimal used = balance.getUsedDays() != null ? balance.getUsedDays() : BigDecimal.ZERO;

            balance.setPendingDays(pending.subtract(request.getTotalDays()).max(BigDecimal.ZERO));
            balance.setUsedDays(used.add(request.getTotalDays()));
            balanceRepository.save(balance);
        }
    }

    /**
     * Hoàn lại số ngày pending (hoặc used nếu đã duyệt) khi đơn bị REJECTED hoặc CANCELLED.
     */
    @Transactional
    public void rollbackLeave(LeaveRequest request, boolean wasApproved) {
        int year = request.getStartDate().getYear();
        LeaveBalance balance = balanceRepository
                .findByCompanyIdAndEmployeeIdAndLeaveTypeIdAndYear(request.getCompanyId(), request.getEmployeeId(), request.getLeaveTypeId(), year)
                .orElse(null);

        if (balance != null) {
            if (wasApproved) {
                BigDecimal used = balance.getUsedDays() != null ? balance.getUsedDays() : BigDecimal.ZERO;
                balance.setUsedDays(used.subtract(request.getTotalDays()).max(BigDecimal.ZERO));
            } else {
                BigDecimal pending = balance.getPendingDays() != null ? balance.getPendingDays() : BigDecimal.ZERO;
                balance.setPendingDays(pending.subtract(request.getTotalDays()).max(BigDecimal.ZERO));
            }
            balanceRepository.save(balance);
        }
    }

    private void applyDataScope(LeaveBalanceFilter filter) {
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

    @Transactional(readOnly = true)
    public BigDecimal getTotalRemainingLeaveDays(UUID employeeId, int year) {
        if (employeeId == null) {
            return BigDecimal.ZERO;
        }
        List<LeaveBalance> balances = balanceRepository.findAllByEmployeeIdAndYear(employeeId, year);
        return balances.stream()
                .map(LeaveBalance::getRemainingDays)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

