package com.cyclosa.payroll.service;

import com.cyclosa.common.enums.DataScope;
import com.cyclosa.common.exception.AppException;
import com.cyclosa.common.response.PageData;
import com.cyclosa.common.security.SecurityPermissionEvaluator;
import com.cyclosa.common.util.SecurityUtils;
import com.cyclosa.employee.dto.response.EmployeeDetailResponse;
import com.cyclosa.employee.service.EmployeeService;
import com.cyclosa.payroll.dto.request.AdjustPayrollRecordRequest;
import com.cyclosa.payroll.dto.request.PayrollRecordFilter;
import com.cyclosa.payroll.dto.response.PayrollRecordDetailResponse;
import com.cyclosa.payroll.dto.response.PayrollRecordResponse;
import com.cyclosa.payroll.dto.response.PayslipResponse;
import com.cyclosa.payroll.entity.PayrollPeriod;
import com.cyclosa.payroll.entity.PayrollRecord;
import com.cyclosa.payroll.entity.PayrollRecordItem;
import com.cyclosa.payroll.enums.PayrollPeriodStatus;
import com.cyclosa.payroll.exception.PayrollErrorCode;
import com.cyclosa.payroll.mapper.PayrollRecordMapper;
import com.cyclosa.payroll.repository.PayrollPeriodRepository;
import com.cyclosa.payroll.repository.PayrollRecordItemRepository;
import com.cyclosa.payroll.repository.PayrollRecordRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayrollRecordService {

    private final PayrollRecordRepository recordRepository;
    private final PayrollRecordItemRepository itemRepository;
    private final PayrollPeriodRepository periodRepository;
    private final PayrollCalculationEngine calculationEngine;
    private final EmployeeService employeeService;
    private final SecurityPermissionEvaluator permEvaluator;
    private final PayrollRecordMapper recordMapper;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("grossSalary", "netSalary", "actualWorkDays", "createdAt");

    @Transactional(readOnly = true)
    public PageData<PayrollRecordResponse> getPayrollRecords(UUID companyId, PayrollRecordFilter filter, Pageable pageable) {
        if (filter == null) {
            filter = new PayrollRecordFilter();
        }
        UUID effectiveCompanyId = filter.getCompanyId() != null ? filter.getCompanyId() : companyId;
        if (effectiveCompanyId == null) {
            throw AppException.badRequest("Yêu cầu cung cấp ID công ty");
        }
        filter.setCompanyId(effectiveCompanyId);

        applyDataScope(filter);

        final PayrollRecordFilter f = filter;
        Specification<PayrollRecord> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("companyId"), f.getCompanyId()));

            if (f.getPayrollPeriodId() != null) {
                predicates.add(cb.equal(root.get("payrollPeriod").get("id"), f.getPayrollPeriodId()));
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
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Pageable effectivePageable = (pageable != null && pageable.isPaged()) ? pageable : filter.toPageable("createdAt", ALLOWED_SORT_FIELDS);
        Page<PayrollRecord> page = recordRepository.findAll(spec, effectivePageable);
        if (page.isEmpty()) {
            return PageData.of(page, List.of());
        }

        Set<UUID> empIds = page.getContent().stream().map(PayrollRecord::getEmployeeId).collect(java.util.stream.Collectors.toSet());
        Map<UUID, com.cyclosa.common.dto.summary.EmployeeSummary> empMap = employeeService.getEmployeeSummaries(empIds);

        List<PayrollRecordResponse> list = page.getContent().stream().map(r -> {
            PayrollRecordResponse res = recordMapper.toResponse(r);
            res.setEmployee(empMap.get(r.getEmployeeId()));
            return res;
        }).toList();

        return PageData.of(page, list);
    }

    @Transactional(readOnly = true)
    public PayrollRecordDetailResponse getPayrollRecordById(UUID companyId, UUID id) {
        PayrollRecord record = recordRepository.findByIdAndCompanyIdWithItems(id, companyId)
                .orElseThrow(() -> new AppException(PayrollErrorCode.PAYROLL_RECORD_NOT_FOUND));

        PayrollRecordDetailResponse res = recordMapper.toDetailResponse(record);
        res.setEmployee(employeeService.getEmployeeSummary(record.getEmployeeId()));
        return res;
    }

    @Transactional(readOnly = true)
    public List<PayslipResponse> getMyPayslips(UUID currentUserId) {
        UUID employeeId = employeeService.findEmployeeIdByUserId(currentUserId)
                .orElseThrow(() -> new AppException(PayrollErrorCode.CURRENT_USER_NOT_EMPLOYEE));

        List<PayrollRecord> records = recordRepository.findAllByEmployeeIdOrderByCreatedAtDesc(employeeId);
        com.cyclosa.common.dto.summary.EmployeeSummary empSummary = employeeService.getEmployeeSummary(employeeId);

        return records.stream().map(r -> {
            PayslipResponse res = recordMapper.toPayslipResponse(r);
            res.setEmployee(empSummary);
            return res;
        }).toList();
    }

    @Transactional
    public PayrollRecordDetailResponse adjustPayrollRecord(UUID companyId, UUID id, AdjustPayrollRecordRequest req) {
        PayrollRecord record = recordRepository.findByIdAndCompanyIdWithItems(id, companyId)
                .orElseThrow(() -> new AppException(PayrollErrorCode.PAYROLL_RECORD_NOT_FOUND));

        PayrollPeriod period = record.getPayrollPeriod();
        if (period.getStatus() == PayrollPeriodStatus.CLOSED) {
            throw new AppException(PayrollErrorCode.PAYROLL_PERIOD_LOCKED);
        }

        // Tạo item điều chỉnh
        PayrollRecordItem item = PayrollRecordItem.builder()
                .payrollRecord(record)
                .salaryComponentId(req.getSalaryComponentId())
                .salaryComponentName(req.getName())
                .componentType(req.getComponentType())
                .amount(req.getAmount())
                .isTaxable(req.getIsTaxable())
                .isInsuranceBase(false)
                .note(req.getNote())
                .build();

        record.getItems().add(item);

        // Tính lại số liệu cho record
        BigDecimal newAllowances = record.getAllowancesTotal();
        BigDecimal newBonus = record.getBonusTotal();

        switch (req.getComponentType()) {
            case ALLOWANCE -> newAllowances = newAllowances.add(req.getAmount());
            case BONUS -> newBonus = newBonus.add(req.getAmount());
            default -> {}
        }

        BigDecimal gross = record.getTimeBasedSalary().add(newAllowances).add(newBonus);
        BigDecimal net = calculationEngine.calculateNetSalary(
                gross, record.getSocialInsuranceEmployee(), record.getPersonalIncomeTax(),
                record.getAdvanceDeduction(), record.getOtherDeductions()
        );

        record.setAllowancesTotal(newAllowances);
        record.setBonusTotal(newBonus);
        record.setGrossSalary(gross);
        record.setNetSalary(net);

        record = recordRepository.save(record);
        log.info("Adjusted PayrollRecord id={}, added item={}", id, req.getName());

        PayrollRecordDetailResponse res = recordMapper.toDetailResponse(record);
        res.setEmployee(employeeService.getEmployeeSummary(record.getEmployeeId()));
        return res;
    }

    private void applyDataScope(PayrollRecordFilter filter) {
        DataScope scope = permEvaluator.getDataScope("payroll.view").orElse(DataScope.OWN);
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
