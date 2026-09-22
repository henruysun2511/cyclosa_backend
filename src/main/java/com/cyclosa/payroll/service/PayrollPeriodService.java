package com.cyclosa.payroll.service;

import com.cyclosa.attendance.service.TimesheetService;
import com.cyclosa.common.dto.summary.CompanySummary;
import com.cyclosa.common.dto.summary.EmployeeAttendanceSummary;
import com.cyclosa.common.dto.summary.EmployeeSummary;
import com.cyclosa.common.exception.AppException;
import com.cyclosa.common.response.PageData;
import com.cyclosa.common.util.PageableUtils;
import com.cyclosa.contract.dto.response.ContractResponse;
import com.cyclosa.contract.service.ContractService;
import com.cyclosa.employee.dto.response.EmployeeDetailResponse;
import com.cyclosa.employee.service.EmployeeService;
import com.cyclosa.organization.service.CompanyService;
import com.cyclosa.payroll.dto.request.CreatePayrollPeriodRequest;
import com.cyclosa.payroll.dto.request.PayrollPeriodFilter;
import com.cyclosa.payroll.dto.request.ProcessPayrollRequest;
import com.cyclosa.payroll.dto.request.UpdatePayrollPeriodRequest;
import com.cyclosa.payroll.dto.response.PayrollPeriodDetailResponse;
import com.cyclosa.payroll.dto.response.PayrollPeriodResponse;
import com.cyclosa.payroll.entity.*;
import com.cyclosa.payroll.enums.ComponentType;
import com.cyclosa.payroll.enums.PayrollPeriodStatus;
import com.cyclosa.payroll.enums.PayrollRecordStatus;
import com.cyclosa.payroll.enums.SalaryAdvanceStatus;
import com.cyclosa.payroll.exception.PayrollErrorCode;
import com.cyclosa.payroll.mapper.PayrollPeriodMapper;
import com.cyclosa.payroll.repository.*;
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
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayrollPeriodService {

    private final PayrollPeriodRepository periodRepository;
    private final PayrollRecordRepository recordRepository;
    private final PayrollRecordItemRepository itemRepository;
    private final SalaryAdvanceRepository advanceRepository;
    private final SalaryComponentRepository componentRepository;

    private final PayrollCalculationEngine calculationEngine;
    private final TimesheetService timesheetService;
    private final ContractService contractService;
    private final EmployeeService employeeService;
    private final CompanyService companyService;
    private final WorkflowEngineService workflowEngineService;
    private final PayrollPeriodMapper periodMapper;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("name", "code", "month", "year", "createdAt");

    @Transactional(readOnly = true)
    public PageData<PayrollPeriodResponse> getPeriods(UUID companyId, PayrollPeriodFilter filter, Pageable pageable) {
        if (filter == null) {
            filter = new PayrollPeriodFilter();
        }
        UUID effectiveCompanyId = filter.getCompanyId() != null ? filter.getCompanyId() : companyId;
        if (effectiveCompanyId == null) {
            throw AppException.badRequest("Yêu cầu cung cấp ID công ty");
        }
        filter.setCompanyId(effectiveCompanyId);

        final PayrollPeriodFilter f = filter;
        Specification<PayrollPeriod> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("companyId"), f.getCompanyId()));

            if (f.getMonth() != null) {
                predicates.add(cb.equal(root.get("month"), f.getMonth()));
            }
            if (f.getYear() != null) {
                predicates.add(cb.equal(root.get("year"), f.getYear()));
            }
            if (f.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), f.getStatus()));
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
        Page<PayrollPeriod> page = periodRepository.findAll(spec, effectivePageable);
        return PageData.of(page, periodMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public PayrollPeriodDetailResponse getPeriodById(UUID companyId, UUID id) {
        PayrollPeriod period = periodRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new AppException(PayrollErrorCode.PAYROLL_PERIOD_NOT_FOUND));

        PayrollPeriodDetailResponse res = periodMapper.toDetailResponse(period);
        if (period.getCompanyId() != null) {
            CompanySummary cs = companyService.getCompanySummary(period.getCompanyId());
            res.setCompany(cs);
        }
        List<PayrollRecord> records = recordRepository.findAllByPayrollPeriodId(id);
        res.setTotalRecordsCount(records.size());

        if (period.getWorkflowInstanceId() != null) {
            try {
                res.setWorkflowHistory(workflowEngineService.getInstanceHistory(period.getWorkflowInstanceId()));
            } catch (Exception ex) {
                log.debug("Workflow history not loaded: {}", ex.getMessage());
            }
        }
        return res;
    }

    @Transactional
    public PayrollPeriodResponse createPeriod(UUID companyId, CreatePayrollPeriodRequest req) {
        if (companyId == null) {
            throw AppException.badRequest("Yêu cầu cung cấp ID công ty");
        }
        if (periodRepository.existsByCodeAndCompanyId(req.getCode(), companyId)) {
            throw new AppException(PayrollErrorCode.PAYROLL_PERIOD_CODE_EXISTS);
        }
        if (periodRepository.existsByCompanyIdAndMonthAndYear(companyId, req.getMonth(), req.getYear())) {
            throw AppException.conflict(String.format("Kỳ lương tháng %d/%d đã tồn tại trong công ty", req.getMonth(), req.getYear()));
        }

        PayrollPeriod period = periodMapper.toEntity(req);
        period.setCompanyId(companyId);
        period.setStatus(PayrollPeriodStatus.OPEN);
        period.setTotalGross(BigDecimal.ZERO);
        period.setTotalNet(BigDecimal.ZERO);
        period = periodRepository.save(period);

        log.info("Created PayrollPeriod id={}, code={}, companyId={}", period.getId(), period.getCode(), companyId);
        return periodMapper.toResponse(period);
    }

    @Transactional
    public PayrollPeriodResponse updatePeriod(UUID companyId, UUID id, UpdatePayrollPeriodRequest req) {
        PayrollPeriod period = periodRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new AppException(PayrollErrorCode.PAYROLL_PERIOD_NOT_FOUND));

        if (period.getStatus() == PayrollPeriodStatus.CLOSED) {
            throw new AppException(PayrollErrorCode.PAYROLL_PERIOD_LOCKED);
        }

        periodMapper.updateEntityFromRequest(req, period);
        period = periodRepository.save(period);
        return periodMapper.toResponse(period);
    }

    /**
     * Khởi chạy thuật toán tính toán tự động toàn bộ nhân sự trong kỳ lương.
     */
    @Transactional
    public PayrollPeriodDetailResponse processPayroll(UUID companyId, UUID periodId, ProcessPayrollRequest req) {
        PayrollPeriod period = periodRepository.findByIdAndCompanyId(periodId, companyId)
                .orElseThrow(() -> new AppException(PayrollErrorCode.PAYROLL_PERIOD_NOT_FOUND));

        if (period.getStatus() == PayrollPeriodStatus.CLOSED) {
            throw new AppException(PayrollErrorCode.PAYROLL_PERIOD_LOCKED);
        }

        // 1. Thu thập dữ liệu công từ TimesheetService
        Set<UUID> targetEmpIds = (req != null && req.getEmployeeIds() != null && !req.getEmployeeIds().isEmpty())
                ? req.getEmployeeIds() : null;

        Map<UUID, EmployeeAttendanceSummary> attendanceMap = timesheetService.getMonthlyAttendanceSummaries(
                companyId, period.getMonth(), period.getYear(), targetEmpIds
        );

        // 2. Thu thập HĐLĐ active từ ContractService
        Map<UUID, ContractResponse> contractMap = contractService.getActiveContractsByCompany(companyId);

        // Xác định tập hợp nhân viên cần tính lương
        Set<UUID> employeeIdsToProcess = new HashSet<>();
        if (targetEmpIds != null) {
            employeeIdsToProcess.addAll(targetEmpIds);
        } else {
            employeeIdsToProcess.addAll(attendanceMap.keySet());
            employeeIdsToProcess.addAll(contractMap.keySet());
        }

        if (employeeIdsToProcess.isEmpty()) {
            log.info("Không có nhân sự nào để tính lương cho kỳ id={}", periodId);
            return getPeriodById(companyId, periodId);
        }

        // Xóa bản ghi lương cũ nếu đang tính lại
        if (targetEmpIds == null) {
            recordRepository.deleteAllByPayrollPeriodId(periodId);
        }

        BigDecimal periodTotalGross = BigDecimal.ZERO;
        BigDecimal periodTotalNet = BigDecimal.ZERO;
        List<PayrollRecord> savedRecords = new ArrayList<>();

        for (UUID empId : employeeIdsToProcess) {
            ContractResponse contract = contractMap.get(empId);
            BigDecimal basicSalary = (contract != null && contract.getBasicSalary() != null)
                    ? contract.getBasicSalary() : BigDecimal.ZERO;
            BigDecimal insuranceSalary = (contract != null && contract.getInsuranceSalary() != null)
                    ? contract.getInsuranceSalary() : basicSalary;

            EmployeeAttendanceSummary att = attendanceMap.get(empId);
            BigDecimal stdDays = period.getStandardWorkDays();
            BigDecimal actualDays = att != null ? att.getActualWorkDays() : BigDecimal.ZERO;
            BigDecimal paidLeaveDays = att != null ? att.getPaidLeaveDays() : BigDecimal.ZERO;

            // 1. Lương thời gian
            BigDecimal timeBasedSalary = calculationEngine.calculateTimeBasedSalary(basicSalary, stdDays, actualDays, paidLeaveDays);

            // 2. Phụ cấp từ hợp đồng
            BigDecimal allowancesTotal = BigDecimal.ZERO;
            List<PayrollRecordItem> items = new ArrayList<>();

            if (contract != null) {
                if (contract.getAllowanceLunch() != null && contract.getAllowanceLunch().compareTo(BigDecimal.ZERO) > 0) {
                    allowancesTotal = allowancesTotal.add(contract.getAllowanceLunch());
                    items.add(PayrollRecordItem.builder()
                            .salaryComponentName("Phụ cấp ăn trưa")
                            .salaryComponentCode("LUNCH_ALLOWANCE")
                            .componentType(ComponentType.ALLOWANCE)
                            .amount(contract.getAllowanceLunch())
                            .isTaxable(contract.getAllowanceLunch().compareTo(PayrollCalculationEngine.MAX_TAX_EXEMPT_LUNCH) > 0)
                            .isInsuranceBase(false)
                            .build());
                }
                if (contract.getAllowancePhone() != null && contract.getAllowancePhone().compareTo(BigDecimal.ZERO) > 0) {
                    allowancesTotal = allowancesTotal.add(contract.getAllowancePhone());
                    items.add(PayrollRecordItem.builder()
                            .salaryComponentName("Phụ cấp điện thoại")
                            .salaryComponentCode("PHONE_ALLOWANCE")
                            .componentType(ComponentType.ALLOWANCE)
                            .amount(contract.getAllowancePhone())
                            .isTaxable(false)
                            .isInsuranceBase(false)
                            .build());
                }
                if (contract.getAllowanceTransport() != null && contract.getAllowanceTransport().compareTo(BigDecimal.ZERO) > 0) {
                    allowancesTotal = allowancesTotal.add(contract.getAllowanceTransport());
                    items.add(PayrollRecordItem.builder()
                            .salaryComponentName("Phụ cấp xăng xe đi lại")
                            .salaryComponentCode("TRANSPORT_ALLOWANCE")
                            .componentType(ComponentType.ALLOWANCE)
                            .amount(contract.getAllowanceTransport())
                            .isTaxable(false)
                            .isInsuranceBase(false)
                            .build());
                }
                if (contract.getAllowanceOther() != null && contract.getAllowanceOther().compareTo(BigDecimal.ZERO) > 0) {
                    allowancesTotal = allowancesTotal.add(contract.getAllowanceOther());
                    items.add(PayrollRecordItem.builder()
                            .salaryComponentName("Phụ cấp khác")
                            .salaryComponentCode("OTHER_ALLOWANCE")
                            .componentType(ComponentType.ALLOWANCE)
                            .amount(contract.getAllowanceOther())
                            .isTaxable(true)
                            .isInsuranceBase(false)
                            .build());
                }
            }

            // 3. Tổng thu nhập Gross
            BigDecimal grossSalary = timeBasedSalary.add(allowancesTotal);

            // 4. Trích nộp bảo hiểm
            BigDecimal socialInsuranceEmployee = calculationEngine.calculateSocialInsuranceEmployee(insuranceSalary);
            BigDecimal socialInsuranceEmployer = calculationEngine.calculateSocialInsuranceEmployer(insuranceSalary);

            // 5. Thu nhập chịu thuế TNCN
            BigDecimal taxExemptLunch = (contract != null && contract.getAllowanceLunch() != null)
                    ? contract.getAllowanceLunch().min(PayrollCalculationEngine.MAX_TAX_EXEMPT_LUNCH)
                    : BigDecimal.ZERO;
            BigDecimal taxableIncome = grossSalary.subtract(taxExemptLunch).max(BigDecimal.ZERO);

            // 6. Giảm trừ gia cảnh
            int dependentsCount = employeeService.countDependents(empId);
            BigDecimal personalRelief = PayrollCalculationEngine.PERSONAL_RELIEF;
            BigDecimal depRelief = PayrollCalculationEngine.DEPENDENT_RELIEF_PER_PERSON.multiply(BigDecimal.valueOf(dependentsCount));
            BigDecimal totalRelief = personalRelief.add(depRelief);

            // 7. Thu nhập tính thuế & Thuế TNCN
            BigDecimal assessedIncome = taxableIncome.subtract(socialInsuranceEmployee).subtract(totalRelief).max(BigDecimal.ZERO);
            BigDecimal pit = calculationEngine.calculatePersonalIncomeTax(assessedIncome);

            // 8. Khấu trừ tạm ứng lương
            List<SalaryAdvance> advances = advanceRepository.findAllByCompanyIdAndEmployeeIdAndStatus(
                    companyId, empId, SalaryAdvanceStatus.DISBURSED
            );
            BigDecimal advanceDeduction = advances.stream()
                    .map(SalaryAdvance::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 9. Lương thực nhận (Net)
            BigDecimal netSalary = calculationEngine.calculateNetSalary(
                    grossSalary, socialInsuranceEmployee, pit, advanceDeduction, BigDecimal.ZERO
            );

            // 10. Snapshot tài khoản ngân hàng
            EmployeeDetailResponse empDetail = null;
            try {
                empDetail = employeeService.getEmployeeByIdInternal(empId);
            } catch (Exception ex) {
                log.debug("Employee detail not loaded: {}", ex.getMessage());
            }

            String bankAcc = empDetail != null ? empDetail.getBankAccountNumber() : null;
            String bankName = empDetail != null ? empDetail.getBankName() : null;
            String bankBranch = empDetail != null ? empDetail.getBankBranch() : null;

            PayrollRecord rec = PayrollRecord.builder()
                    .companyId(companyId)
                    .payrollPeriod(period)
                    .employeeId(empId)
                    .basicSalary(basicSalary)
                    .insuranceSalary(insuranceSalary)
                    .standardWorkDays(stdDays)
                    .actualWorkDays(actualDays)
                    .paidLeaveDays(paidLeaveDays)
                    .timeBasedSalary(timeBasedSalary)
                    .allowancesTotal(allowancesTotal)
                    .bonusTotal(BigDecimal.ZERO)
                    .grossSalary(grossSalary)
                    .socialInsuranceEmployee(socialInsuranceEmployee)
                    .socialInsuranceEmployer(socialInsuranceEmployer)
                    .taxableIncome(taxableIncome)
                    .dependentsCount(dependentsCount)
                    .personalReliefAmount(personalRelief)
                    .dependentReliefAmount(depRelief)
                    .assessedIncome(assessedIncome)
                    .personalIncomeTax(pit)
                    .advanceDeduction(advanceDeduction)
                    .otherDeductions(BigDecimal.ZERO)
                    .netSalary(netSalary)
                    .bankAccountNumber(bankAcc)
                    .bankName(bankName)
                    .bankBranch(bankBranch)
                    .status(PayrollRecordStatus.DRAFT)
                    .build();

            for (PayrollRecordItem item : items) {
                item.setPayrollRecord(rec);
            }
            rec.setItems(items);

            rec = recordRepository.save(rec);
            savedRecords.add(rec);

            periodTotalGross = periodTotalGross.add(grossSalary);
            periodTotalNet = periodTotalNet.add(netSalary);
        }

        period.setStatus(PayrollPeriodStatus.PROCESSING);
        period.setTotalGross(periodTotalGross);
        period.setTotalNet(periodTotalNet);
        period = periodRepository.save(period);
        log.info("Processed payroll for period id={}, totalRecords={}, totalGross={}, totalNet={}",
                periodId, savedRecords.size(), periodTotalGross, periodTotalNet);

        return getPeriodById(companyId, periodId);
    }

    @Transactional
    public PayrollPeriodResponse submitApproval(UUID companyId, UUID periodId) {
        PayrollPeriod period = periodRepository.findByIdAndCompanyId(periodId, companyId)
                .orElseThrow(() -> new AppException(PayrollErrorCode.PAYROLL_PERIOD_NOT_FOUND));

        if (period.getStatus() == PayrollPeriodStatus.OPEN) {
            throw new AppException(PayrollErrorCode.PAYROLL_NOT_YET_PROCESSED);
        }
        if (period.getStatus() == PayrollPeriodStatus.CLOSED || period.getStatus() == PayrollPeriodStatus.APPROVED) {
            throw new AppException(PayrollErrorCode.PAYROLL_ALREADY_APPROVED);
        }

        // Khởi tạo luồng duyệt qua WorkflowEngineService
        try {
            StartWorkflowRequest wfReq = StartWorkflowRequest.builder()
                    .companyId(companyId)
                    .requestType(ApprovalRequestType.PAYROLL_APPROVAL)
                    .requestId(period.getId())
                    .contextVariables(Map.of(
                            "periodCode", period.getCode(),
                            "month", period.getMonth().toString(),
                            "year", period.getYear().toString(),
                            "totalGross", period.getTotalGross().toString(),
                            "totalNet", period.getTotalNet().toString()
                    ))
                    .build();

            WorkflowInstanceResponse instance = workflowEngineService.startWorkflow(wfReq);
            period.setWorkflowInstanceId(instance.getId());
            period = periodRepository.save(period);
            log.info("Submitted payroll approval workflow instance id={} for period id={}", instance.getId(), periodId);
        } catch (Exception ex) {
            log.warn("Chưa thể khởi chạy workflow duyệt tự động: {}", ex.getMessage());
        }

        return periodMapper.toResponse(period);
    }

    @Transactional
    public PayrollPeriodResponse approvePeriod(UUID companyId, UUID periodId) {
        PayrollPeriod period = periodRepository.findByIdAndCompanyId(periodId, companyId)
                .orElseThrow(() -> new AppException(PayrollErrorCode.PAYROLL_PERIOD_NOT_FOUND));

        if (period.getStatus() == PayrollPeriodStatus.OPEN) {
            throw new AppException(PayrollErrorCode.PAYROLL_NOT_YET_PROCESSED);
        }

        period.setStatus(PayrollPeriodStatus.APPROVED);
        period = periodRepository.save(period);

        List<PayrollRecord> records = recordRepository.findAllByPayrollPeriodId(periodId);
        for (PayrollRecord rec : records) {
            rec.setStatus(PayrollRecordStatus.APPROVED);
        }
        recordRepository.saveAll(records);
        log.info("Approved PayrollPeriod id={}", periodId);

        return periodMapper.toResponse(period);
    }

    @Transactional
    public PayrollPeriodResponse closePeriod(UUID companyId, UUID periodId) {
        PayrollPeriod period = periodRepository.findByIdAndCompanyId(periodId, companyId)
                .orElseThrow(() -> new AppException(PayrollErrorCode.PAYROLL_PERIOD_NOT_FOUND));

        period.setStatus(PayrollPeriodStatus.CLOSED);
        period = periodRepository.save(period);

        List<PayrollRecord> records = recordRepository.findAllByPayrollPeriodId(periodId);
        for (PayrollRecord rec : records) {
            rec.setStatus(PayrollRecordStatus.PAID);

            // Cập nhật các đơn tạm ứng đã được khấu trừ trong kỳ này
            List<SalaryAdvance> advances = advanceRepository.findAllByCompanyIdAndEmployeeIdAndStatus(
                    companyId, rec.getEmployeeId(), SalaryAdvanceStatus.DISBURSED
            );
            for (SalaryAdvance adv : advances) {
                adv.setStatus(SalaryAdvanceStatus.DEDUCTED);
                adv.setDeductedPayrollPeriodId(periodId);
            }
            advanceRepository.saveAll(advances);
        }
        recordRepository.saveAll(records);
        log.info("Closed PayrollPeriod id={}", periodId);

        return periodMapper.toResponse(period);
    }
}
