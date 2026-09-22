package com.cyclosa.payroll.service;

import com.cyclosa.common.enums.DataScope;
import com.cyclosa.common.exception.AppException;
import com.cyclosa.common.response.PageData;
import com.cyclosa.common.security.SecurityPermissionEvaluator;
import com.cyclosa.common.util.SecurityUtils;
import com.cyclosa.contract.dto.response.ContractResponse;
import com.cyclosa.contract.service.ContractService;
import com.cyclosa.employee.dto.response.EmployeeDetailResponse;
import com.cyclosa.employee.service.EmployeeService;
import com.cyclosa.organization.service.CompanyService;
import com.cyclosa.payroll.dto.request.CreateSalaryAdvanceRequest;
import com.cyclosa.payroll.dto.request.SalaryAdvanceFilter;
import com.cyclosa.payroll.dto.response.SalaryAdvanceDetailResponse;
import com.cyclosa.payroll.dto.response.SalaryAdvanceResponse;
import com.cyclosa.payroll.entity.SalaryAdvance;
import com.cyclosa.payroll.enums.SalaryAdvanceStatus;
import com.cyclosa.payroll.exception.PayrollErrorCode;
import com.cyclosa.payroll.mapper.SalaryAdvanceMapper;
import com.cyclosa.payroll.repository.SalaryAdvanceRepository;
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
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SalaryAdvanceService {

    private final SalaryAdvanceRepository advanceRepository;
    private final EmployeeService employeeService;
    private final ContractService contractService;
    private final CompanyService companyService;
    private final WorkflowEngineService workflowEngineService;
    private final SecurityPermissionEvaluator permEvaluator;
    private final SalaryAdvanceMapper advanceMapper;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("requestDate", "amount", "status", "createdAt");

    @Transactional
    public SalaryAdvanceResponse createAdvance(UUID currentUserId, CreateSalaryAdvanceRequest req) {
        UUID employeeId = employeeService.findEmployeeIdByUserId(currentUserId)
                .orElseThrow(() -> new AppException(PayrollErrorCode.CURRENT_USER_NOT_EMPLOYEE));

        EmployeeDetailResponse emp = employeeService.getEmployeeByIdInternal(employeeId);
        UUID companyId = emp.getCompany() != null ? emp.getCompany().getId() : null;
        if (companyId == null) {
            throw AppException.badRequest("Nhân viên chưa thuộc công ty nào");
        }

        // Kiểm tra hạn mức tạm ứng (tối đa 50% lương cơ bản)
        Optional<ContractResponse> contractOpt = contractService.getActiveContractByEmployee(employeeId);
        BigDecimal basicSalary = contractOpt.map(ContractResponse::getBasicSalary).orElse(BigDecimal.ZERO);
        if (basicSalary.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal maxAdvance = basicSalary.multiply(BigDecimal.valueOf(0.50));
            if (req.getAmount().compareTo(maxAdvance) > 0) {
                throw new AppException(PayrollErrorCode.SALARY_ADVANCE_EXCEEDS_LIMIT,
                        String.format("Số tiền xin tạm ứng (%,.0f đ) vượt quá 50%% lương cơ bản (%,.0f đ)",
                                req.getAmount().doubleValue(), maxAdvance.doubleValue()));
            }
        }

        SalaryAdvance advance = advanceMapper.toEntity(req);
        advance.setCompanyId(companyId);
        advance.setEmployeeId(employeeId);
        advance.setStatus(SalaryAdvanceStatus.PENDING_APPROVAL);
        advance = advanceRepository.save(advance);

        // Khởi tạo luồng duyệt qua Workflow
        try {
            StartWorkflowRequest wfReq = StartWorkflowRequest.builder()
                    .companyId(companyId)
                    .requestType(ApprovalRequestType.SALARY_ADVANCE)
                    .requestId(advance.getId())
                    .requesterEmployeeId(employeeId)
                    .contextVariables(Map.of(
                            "amount", req.getAmount().toString(),
                            "requestDate", req.getRequestDate().toString()
                    ))
                    .build();

            WorkflowInstanceResponse instance = workflowEngineService.startWorkflow(wfReq);
            advance.setWorkflowInstanceId(instance.getId());
            advance = advanceRepository.save(advance);
            log.info("Started workflow instance id={} for SalaryAdvance id={}", instance.getId(), advance.getId());
        } catch (Exception ex) {
            log.warn("Chưa thể khởi chạy workflow tự động cho đơn tạm ứng: {}", ex.getMessage());
        }

        SalaryAdvanceResponse res = advanceMapper.toResponse(advance);
        res.setEmployee(employeeService.getEmployeeSummary(employeeId));
        return res;
    }

    @Transactional(readOnly = true)
    public PageData<SalaryAdvanceResponse> getMyAdvances(UUID currentUserId, SalaryAdvanceFilter filter, Pageable pageable) {
        UUID employeeId = employeeService.findEmployeeIdByUserId(currentUserId)
                .orElseThrow(() -> new AppException(PayrollErrorCode.CURRENT_USER_NOT_EMPLOYEE));

        if (filter == null) {
            filter = new SalaryAdvanceFilter();
        }
        filter.setExactEmployeeId(employeeId);
        return searchAdvances(filter, pageable);
    }

    @Transactional(readOnly = true)
    public PageData<SalaryAdvanceResponse> getAdvances(UUID companyId, SalaryAdvanceFilter filter, Pageable pageable) {
        if (filter == null) {
            filter = new SalaryAdvanceFilter();
        }
        UUID effectiveCompanyId = filter.getCompanyId() != null ? filter.getCompanyId() : companyId;
        if (effectiveCompanyId == null) {
            throw AppException.badRequest("Yêu cầu cung cấp ID công ty");
        }
        filter.setCompanyId(effectiveCompanyId);

        applyDataScope(filter);
        return searchAdvances(filter, pageable);
    }

    @Transactional(readOnly = true)
    public SalaryAdvanceDetailResponse getAdvanceById(UUID companyId, UUID id) {
        SalaryAdvance advance = advanceRepository.findById(id)
                .orElseThrow(() -> new AppException(PayrollErrorCode.SALARY_ADVANCE_NOT_FOUND));

        if (companyId != null && !advance.getCompanyId().equals(companyId)) {
            throw new AppException(PayrollErrorCode.SALARY_ADVANCE_NOT_FOUND);
        }

        SalaryAdvanceDetailResponse res = advanceMapper.toDetailResponse(advance);
        res.setEmployee(employeeService.getEmployeeSummary(advance.getEmployeeId()));
        if (advance.getCompanyId() != null) {
            res.setCompany(companyService.getCompanySummary(advance.getCompanyId()));
        }
        if (advance.getWorkflowInstanceId() != null) {
            try {
                res.setWorkflowHistory(workflowEngineService.getInstanceHistory(advance.getWorkflowInstanceId()));
            } catch (Exception ex) {
                log.debug("Workflow history not loaded: {}", ex.getMessage());
            }
        }
        return res;
    }

    @Transactional
    public SalaryAdvanceResponse disburseAdvance(UUID companyId, UUID id) {
        SalaryAdvance advance = advanceRepository.findById(id)
                .orElseThrow(() -> new AppException(PayrollErrorCode.SALARY_ADVANCE_NOT_FOUND));

        if (companyId != null && !advance.getCompanyId().equals(companyId)) {
            throw new AppException(PayrollErrorCode.SALARY_ADVANCE_NOT_FOUND);
        }

        if (advance.getStatus() != SalaryAdvanceStatus.APPROVED) {
            throw new AppException(PayrollErrorCode.SALARY_ADVANCE_ALREADY_PROCESSED,
                    "Chỉ có thể giải ngân đơn tạm ứng đã được phê duyệt");
        }

        advance.setStatus(SalaryAdvanceStatus.DISBURSED);
        advance.setDisbursedAt(LocalDateTime.now());
        advance = advanceRepository.save(advance);
        log.info("Disbursed SalaryAdvance id={}, amount={}", id, advance.getAmount());

        SalaryAdvanceResponse res = advanceMapper.toResponse(advance);
        res.setEmployee(employeeService.getEmployeeSummary(advance.getEmployeeId()));
        return res;
    }

    @Transactional
    public void handleWorkflowCompleted(UUID advanceId, ApprovalStatus finalStatus) {
        SalaryAdvance advance = advanceRepository.findById(advanceId).orElse(null);
        if (advance == null) {
            log.warn("Không tìm thấy SalaryAdvance id={}", advanceId);
            return;
        }

        if (finalStatus == ApprovalStatus.APPROVED) {
            advance.setStatus(SalaryAdvanceStatus.APPROVED);
        } else if (finalStatus == ApprovalStatus.REJECTED) {
            advance.setStatus(SalaryAdvanceStatus.REJECTED);
        }
        advanceRepository.save(advance);
        log.info("Updated SalaryAdvance id={} status to {}", advanceId, advance.getStatus());
    }

    private PageData<SalaryAdvanceResponse> searchAdvances(SalaryAdvanceFilter filter, Pageable pageable) {
        final SalaryAdvanceFilter f = filter;
        Specification<SalaryAdvance> spec = (root, query, cb) -> {
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
            if (f.getFromDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("requestDate"), f.getFromDate()));
            }
            if (f.getToDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("requestDate"), f.getToDate()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Pageable effectivePageable = (pageable != null && pageable.isPaged()) ? pageable : filter.toPageable("createdAt", ALLOWED_SORT_FIELDS);
        Page<SalaryAdvance> page = advanceRepository.findAll(spec, effectivePageable);
        if (page.isEmpty()) {
            return PageData.of(page, List.of());
        }

        Set<UUID> empIds = page.getContent().stream().map(SalaryAdvance::getEmployeeId).collect(java.util.stream.Collectors.toSet());
        Map<UUID, com.cyclosa.common.dto.summary.EmployeeSummary> empMap = employeeService.getEmployeeSummaries(empIds);

        List<SalaryAdvanceResponse> list = page.getContent().stream().map(a -> {
            SalaryAdvanceResponse res = advanceMapper.toResponse(a);
            res.setEmployee(empMap.get(a.getEmployeeId()));
            return res;
        }).toList();

        return PageData.of(page, list);
    }

    private void applyDataScope(SalaryAdvanceFilter filter) {
        DataScope scope = permEvaluator.getDataScope("payroll.advance").orElse(DataScope.OWN);
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
