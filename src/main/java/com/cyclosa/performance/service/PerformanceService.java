package com.cyclosa.performance.service;

import com.cyclosa.common.dto.summary.CompanySummary;
import com.cyclosa.common.dto.summary.EmployeeSummary;
import com.cyclosa.common.dto.summary.OrgUnitSummary;
import com.cyclosa.common.dto.summary.PositionSummary;
import com.cyclosa.common.enums.DataScope;
import com.cyclosa.common.exception.AppException;
import com.cyclosa.common.response.PageData;
import com.cyclosa.common.security.SecurityPermissionEvaluator;
import com.cyclosa.common.util.PageableUtils;
import com.cyclosa.common.util.SecurityUtils;
import com.cyclosa.employee.dto.response.EmployeeDetailResponse;
import com.cyclosa.employee.service.EmployeeService;
import com.cyclosa.organization.service.CompanyService;
import com.cyclosa.organization.service.OrganizationalUnitService;
import com.cyclosa.organization.service.PositionService;
import com.cyclosa.performance.dto.request.*;
import com.cyclosa.performance.dto.response.*;
import com.cyclosa.performance.entity.*;
import com.cyclosa.performance.enums.*;
import com.cyclosa.performance.exception.PerformanceErrorCode;
import com.cyclosa.performance.mapper.PerformanceMapper;
import com.cyclosa.performance.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PerformanceService {

    private static final Set<String> ALLOWED_CYCLE_SORT_FIELDS = Set.of("createdAt", "updatedAt", "name", "startDate", "endDate", "status");
    private static final Set<String> ALLOWED_KPI_SORT_FIELDS = Set.of("createdAt", "updatedAt", "name", "unit");
    private static final Set<String> ALLOWED_GOAL_SORT_FIELDS = Set.of("createdAt", "updatedAt", "title", "weightPercentage");
    private static final Set<String> ALLOWED_EVALUATION_SORT_FIELDS = Set.of("createdAt", "updatedAt", "status", "finalScore");

    private final PerformanceCycleRepository cycleRepository;
    private final KpiRepository kpiRepository;
    private final GoalRepository goalRepository;
    private final PerformanceEvaluationRepository evaluationRepository;
    private final PerformanceReviewRepository reviewRepository;

    // Public Services from other modules (Strict Modular Monolith: NO cross-module repository injection!)
    private final EmployeeService employeeService;
    private final OrganizationalUnitService orgUnitService;
    private final PositionService positionService;
    private final CompanyService companyService;
    private final SecurityPermissionEvaluator permEvaluator;
    private final PerformanceMapper performanceMapper;

    // =========================================================================
    // 1. Performance Cycles
    // =========================================================================


    @Transactional
    public PerformanceCycleResponse createCycle(CreatePerformanceCycleRequest request) {
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new AppException(PerformanceErrorCode.CYCLE_DATE_INVALID);
        }
        if (request.getCompanyId() != null && cycleRepository.existsByNameAndCompanyId(request.getName(), request.getCompanyId())) {
            throw new AppException(PerformanceErrorCode.CYCLE_NAME_EXISTS);
        }

        PerformanceCycle cycle = performanceMapper.toEntity(request);
        cycle.setStatus(PerformanceCycleStatus.DRAFT);
        cycle = cycleRepository.save(cycle);
        return performanceMapper.toResponse(cycle);
    }


    @Transactional
    public PerformanceCycleResponse updateCycle(UUID id, UpdatePerformanceCycleRequest request) {
        PerformanceCycle cycle = cycleRepository.findById(id)
                .orElseThrow(() -> new AppException(PerformanceErrorCode.CYCLE_NOT_FOUND));

        if (cycle.getStatus() == PerformanceCycleStatus.CLOSED) {
            throw new AppException(PerformanceErrorCode.CYCLE_CANNOT_BE_MODIFIED);
        }

        if (request.getName() != null && !request.getName().isBlank()) {
            cycle.setName(request.getName());
        }
        if (request.getCycleType() != null) {
            cycle.setCycleType(request.getCycleType());
        }
        if (request.getStartDate() != null) {
            cycle.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            cycle.setEndDate(request.getEndDate());
        }
        if (cycle.getStartDate().isAfter(cycle.getEndDate())) {
            throw new AppException(PerformanceErrorCode.CYCLE_DATE_INVALID);
        }
        if (request.getStatus() != null) {
            cycle.setStatus(request.getStatus());
        }
        if (request.getDescription() != null) {
            cycle.setDescription(request.getDescription());
        }

        cycle = cycleRepository.save(cycle);
        return performanceMapper.toResponse(cycle);
    }


    @Transactional(readOnly = true)
    public PerformanceCycleResponse getCycleById(UUID id) {
        PerformanceCycle cycle = cycleRepository.findById(id)
                .orElseThrow(() -> new AppException(PerformanceErrorCode.CYCLE_NOT_FOUND));
        return performanceMapper.toResponse(cycle);
    }


    @Transactional(readOnly = true)
    public PageData<PerformanceCycleResponse> getCycles(PerformanceCycleFilter filter) {
        Pageable pageable = (filter != null)
                ? filter.toPageable("startDate", ALLOWED_CYCLE_SORT_FIELDS)
                : PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "startDate"));

        UUID companyId = filter != null ? filter.getCompanyId() : null;
        PerformanceCycleStatus status = filter != null ? filter.getStatus() : null;
        String search = filter != null ? PageableUtils.normalizeKeyword(filter.getKeyword() != null ? filter.getKeyword() : filter.getSearch()) : null;

        Page<PerformanceCycle> page = cycleRepository.searchCycles(companyId, status, search, pageable);
        return PageData.of(page, performanceMapper::toResponse);
    }


    @Transactional
    public void deleteCycle(UUID id) {
        PerformanceCycle cycle = cycleRepository.findById(id)
                .orElseThrow(() -> new AppException(PerformanceErrorCode.CYCLE_NOT_FOUND));
        if (cycle.getStatus() != PerformanceCycleStatus.DRAFT) {
            throw new AppException(PerformanceErrorCode.CYCLE_CANNOT_BE_MODIFIED);
        }
        cycleRepository.delete(cycle);
    }


    @Transactional
    public PerformanceCycleResponse changeCycleStatus(UUID id, PerformanceCycleStatus status) {
        PerformanceCycle cycle = cycleRepository.findById(id)
                .orElseThrow(() -> new AppException(PerformanceErrorCode.CYCLE_NOT_FOUND));
        cycle.setStatus(status);
        cycle = cycleRepository.save(cycle);
        return performanceMapper.toResponse(cycle);
    }

    // =========================================================================
    // 2. KPIs
    // =========================================================================


    @Transactional
    public KpiResponse createKpi(CreateKpiRequest request) {
        Kpi kpi = performanceMapper.toEntity(request);
        kpi.setIsActive(true);
        kpi = kpiRepository.save(kpi);

        KpiResponse response = performanceMapper.toResponse(kpi);
        if (kpi.getOrganizationalUnitId() != null) {
            OrgUnitSummary unit = orgUnitService.getUnitSummary(kpi.getOrganizationalUnitId());
            response.setOrganizationalUnit(unit);
            if (unit != null) response.setOrganizationalUnitName(unit.getName());
        }
        if (kpi.getCompanyId() != null) {
            response.setCompany(companyService.getCompanySummary(kpi.getCompanyId()));
        }
        return response;
    }


    @Transactional
    public KpiResponse updateKpi(UUID id, UpdateKpiRequest request) {
        Kpi kpi = kpiRepository.findById(id)
                .orElseThrow(() -> new AppException(PerformanceErrorCode.KPI_NOT_FOUND));

        if (request.getOrganizationalUnitId() != null) {
            kpi.setOrganizationalUnitId(request.getOrganizationalUnitId());
        }
        if (request.getName() != null && !request.getName().isBlank()) {
            kpi.setName(request.getName());
        }
        if (request.getUnit() != null && !request.getUnit().isBlank()) {
            kpi.setUnit(request.getUnit());
        }
        if (request.getDescription() != null) {
            kpi.setDescription(request.getDescription());
        }
        if (request.getIsActive() != null) {
            kpi.setIsActive(request.getIsActive());
        }

        kpi = kpiRepository.save(kpi);

        KpiResponse response = performanceMapper.toResponse(kpi);
        if (kpi.getOrganizationalUnitId() != null) {
            OrgUnitSummary unit = orgUnitService.getUnitSummary(kpi.getOrganizationalUnitId());
            response.setOrganizationalUnit(unit);
            if (unit != null) response.setOrganizationalUnitName(unit.getName());
        }
        if (kpi.getCompanyId() != null) {
            response.setCompany(companyService.getCompanySummary(kpi.getCompanyId()));
        }
        return response;
    }


    @Transactional(readOnly = true)
    public KpiResponse getKpiById(UUID id) {
        Kpi kpi = kpiRepository.findById(id)
                .orElseThrow(() -> new AppException(PerformanceErrorCode.KPI_NOT_FOUND));

        KpiResponse response = performanceMapper.toResponse(kpi);
        if (kpi.getOrganizationalUnitId() != null) {
            OrgUnitSummary unit = orgUnitService.getUnitSummary(kpi.getOrganizationalUnitId());
            response.setOrganizationalUnit(unit);
            if (unit != null) response.setOrganizationalUnitName(unit.getName());
        }
        if (kpi.getCompanyId() != null) {
            response.setCompany(companyService.getCompanySummary(kpi.getCompanyId()));
        }
        return response;
    }


    @Transactional(readOnly = true)
    public PageData<KpiResponse> getKpis(KpiFilter filter) {
        Pageable pageable = (filter != null)
                ? filter.toPageable("createdAt", ALLOWED_KPI_SORT_FIELDS)
                : PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"));

        UUID companyId = filter != null ? filter.getCompanyId() : null;
        UUID unitId = filter != null ? filter.getOrganizationalUnitId() : null;
        Boolean isActive = filter != null ? filter.getIsActive() : null;
        String search = filter != null ? PageableUtils.normalizeKeyword(filter.getKeyword() != null ? filter.getKeyword() : filter.getSearch()) : null;

        Page<Kpi> page = kpiRepository.searchKpis(companyId, unitId, isActive, search, pageable);
        if (page.isEmpty()) {
            return PageData.of(page, List.of());
        }

        // Batch Enrichment (Rule 8.3)
        Set<UUID> unitIds = page.getContent().stream()
                .map(Kpi::getOrganizationalUnitId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<UUID, OrgUnitSummary> unitMap = orgUnitService.getUnitSummaries(unitIds);

        Set<UUID> compIds = page.getContent().stream()
                .map(Kpi::getCompanyId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<UUID, CompanySummary> compMap = companyService.getCompanySummaries(compIds);

        List<KpiResponse> content = page.getContent().stream().map(kpi -> {
            KpiResponse res = performanceMapper.toResponse(kpi);
            if (kpi.getOrganizationalUnitId() != null) {
                OrgUnitSummary unit = unitMap.get(kpi.getOrganizationalUnitId());
                res.setOrganizationalUnit(unit);
                if (unit != null) res.setOrganizationalUnitName(unit.getName());
            }
            if (kpi.getCompanyId() != null) {
                res.setCompany(compMap.get(kpi.getCompanyId()));
            }
            return res;
        }).toList();

        return PageData.of(page, content);
    }


    @Transactional
    public void deleteKpi(UUID id) {
        Kpi kpi = kpiRepository.findById(id)
                .orElseThrow(() -> new AppException(PerformanceErrorCode.KPI_NOT_FOUND));
        kpiRepository.delete(kpi);
    }

    // =========================================================================
    // 3. Goals
    // =========================================================================


    @Transactional
    public GoalResponse createGoal(CreateGoalRequest request) {
        EmployeeDetailResponse employee = employeeService.getEmployeeByIdInternal(request.getEmployeeId());
        if (employee == null) {
            throw new AppException(PerformanceErrorCode.EMPLOYEE_NOT_FOUND);
        }

        PerformanceCycle cycle = cycleRepository.findById(request.getPerformanceCycleId())
                .orElseThrow(() -> new AppException(PerformanceErrorCode.CYCLE_NOT_FOUND));

        if (cycle.getStatus() != PerformanceCycleStatus.ACTIVE) {
            throw new AppException(PerformanceErrorCode.CYCLE_CANNOT_BE_MODIFIED);
        }

        if (request.getKpiId() != null) {
            Kpi kpi = kpiRepository.findById(request.getKpiId())
                    .orElseThrow(() -> new AppException(PerformanceErrorCode.KPI_NOT_FOUND));
            if (kpi.getOrganizationalUnitId() != null && employee.getOrganizationalUnit() != null) {
                if (!kpi.getOrganizationalUnitId().equals(employee.getOrganizationalUnit().getId())) {
                    throw new AppException(PerformanceErrorCode.UNAUTHORIZED_ACCESS);
                }
            }
        }

        BigDecimal currentTotal = goalRepository.sumWeightByEmployeeAndCycle(employee.getId(), cycle.getId(), null);
        BigDecimal newTotal = currentTotal.add(request.getWeightPercentage());
        if (newTotal.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new AppException(PerformanceErrorCode.WEIGHT_SUM_INVALID);
        }

        Goal goal = performanceMapper.toEntity(request);
        goal.setEmployeeId(employee.getId());
        goal.setPerformanceCycleId(cycle.getId());
        goal.setStatus(GoalStatus.IN_PROGRESS);
        goal = goalRepository.save(goal);

        GoalResponse response = performanceMapper.toResponse(goal);
        response.setEmployee(employeeService.getEmployeeSummary(employee.getId()));
        if (response.getEmployee() != null) {
            response.setEmployeeName(response.getEmployee().getFullName());
            response.setEmployeeCode(response.getEmployee().getEmployeeCode());
        }
        response.setPerformanceCycleName(cycle.getName());
        if (goal.getKpiId() != null) {
            kpiRepository.findById(goal.getKpiId()).ifPresent(k -> {
                response.setKpiName(k.getName());
                response.setKpiUnit(k.getUnit());
            });
        }
        return response;
    }


    @Transactional
    public GoalResponse updateGoal(UUID id, UpdateGoalRequest request) {
        Goal goal = goalRepository.findById(id)
                .orElseThrow(() -> new AppException(PerformanceErrorCode.GOAL_NOT_FOUND));

        PerformanceCycle cycle = cycleRepository.findById(goal.getPerformanceCycleId())
                .orElseThrow(() -> new AppException(PerformanceErrorCode.CYCLE_NOT_FOUND));

        if (cycle.getStatus() != PerformanceCycleStatus.ACTIVE) {
            throw new AppException(PerformanceErrorCode.CYCLE_CANNOT_BE_MODIFIED);
        }

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            goal.setTitle(request.getTitle());
        }
        if (request.getTargetValue() != null) {
            goal.setTargetValue(request.getTargetValue());
        }
        if (request.getWeightPercentage() != null) {
            BigDecimal currentTotalOther = goalRepository.sumWeightByEmployeeAndCycle(goal.getEmployeeId(), goal.getPerformanceCycleId(), goal.getId());
            BigDecimal newTotal = currentTotalOther.add(request.getWeightPercentage());
            if (newTotal.compareTo(BigDecimal.valueOf(100)) > 0) {
                throw new AppException(PerformanceErrorCode.WEIGHT_SUM_INVALID);
            }
            goal.setWeightPercentage(request.getWeightPercentage());
        }
        if (request.getStatus() != null) {
            goal.setStatus(request.getStatus());
        }
        if (request.getDescription() != null) {
            goal.setDescription(request.getDescription());
        }

        goal = goalRepository.save(goal);

        GoalResponse response = performanceMapper.toResponse(goal);
        response.setEmployee(employeeService.getEmployeeSummary(goal.getEmployeeId()));
        if (response.getEmployee() != null) {
            response.setEmployeeName(response.getEmployee().getFullName());
            response.setEmployeeCode(response.getEmployee().getEmployeeCode());
        }
        response.setPerformanceCycleName(cycle.getName());
        if (goal.getKpiId() != null) {
            kpiRepository.findById(goal.getKpiId()).ifPresent(k -> {
                response.setKpiName(k.getName());
                response.setKpiUnit(k.getUnit());
            });
        }
        return response;
    }


    @Transactional(readOnly = true)
    public GoalResponse getGoalById(UUID id) {
        Goal goal = goalRepository.findById(id)
                .orElseThrow(() -> new AppException(PerformanceErrorCode.GOAL_NOT_FOUND));

        GoalResponse response = performanceMapper.toResponse(goal);
        response.setEmployee(employeeService.getEmployeeSummary(goal.getEmployeeId()));
        if (response.getEmployee() != null) {
            response.setEmployeeName(response.getEmployee().getFullName());
            response.setEmployeeCode(response.getEmployee().getEmployeeCode());
        }
        cycleRepository.findById(goal.getPerformanceCycleId()).ifPresent(c -> response.setPerformanceCycleName(c.getName()));
        if (goal.getKpiId() != null) {
            kpiRepository.findById(goal.getKpiId()).ifPresent(k -> {
                response.setKpiName(k.getName());
                response.setKpiUnit(k.getUnit());
            });
        }
        return response;
    }


    @Transactional(readOnly = true)
    public PageData<GoalResponse> getGoals(GoalFilter filter) {
        Pageable pageable = (filter != null)
                ? filter.toPageable("createdAt", ALLOWED_GOAL_SORT_FIELDS)
                : PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"));

        UUID employeeId = filter != null ? filter.getEmployeeId() : null;
        UUID cycleId = filter != null ? filter.getPerformanceCycleId() : null;

        // DataScope for Goals
        DataScope scope = permEvaluator.getDataScope("performance.view").orElse(DataScope.OWN);
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        UUID currentEmpId = employeeService.findEmployeeIdByUserId(currentUserId).orElse(null);

        if (scope == DataScope.OWN) {
            if (currentEmpId == null) {
                return PageData.of(Page.empty(pageable), List.of());
            }
            employeeId = currentEmpId;
        }

        Page<Goal> page = goalRepository.searchGoals(employeeId, cycleId, pageable);
        if (page.isEmpty()) {
            return PageData.of(page, List.of());
        }

        // Batch Enrichment (Rule 8.3)
        Set<UUID> empIds = page.getContent().stream()
                .map(Goal::getEmployeeId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<UUID, EmployeeSummary> empMap = employeeService.getEmployeeSummaries(empIds);

        Set<UUID> cycleIds = page.getContent().stream()
                .map(Goal::getPerformanceCycleId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<UUID, String> cycleMap = cycleRepository.findAllById(cycleIds).stream()
                .collect(Collectors.toMap(PerformanceCycle::getId, PerformanceCycle::getName));

        Set<UUID> kpiIds = page.getContent().stream()
                .map(Goal::getKpiId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<UUID, Kpi> kpiMap = kpiRepository.findAllById(kpiIds).stream()
                .collect(Collectors.toMap(Kpi::getId, Function.identity()));

        List<GoalResponse> content = page.getContent().stream().map(goal -> {
            GoalResponse res = performanceMapper.toResponse(goal);
            EmployeeSummary emp = empMap.get(goal.getEmployeeId());
            res.setEmployee(emp);
            if (emp != null) {
                res.setEmployeeName(emp.getFullName());
                res.setEmployeeCode(emp.getEmployeeCode());
            }
            res.setPerformanceCycleName(cycleMap.get(goal.getPerformanceCycleId()));
            if (goal.getKpiId() != null) {
                Kpi k = kpiMap.get(goal.getKpiId());
                if (k != null) {
                    res.setKpiName(k.getName());
                    res.setKpiUnit(k.getUnit());
                }
            }
            return res;
        }).toList();

        return PageData.of(page, content);
    }


    @Transactional
    public void deleteGoal(UUID id) {
        Goal goal = goalRepository.findById(id)
                .orElseThrow(() -> new AppException(PerformanceErrorCode.GOAL_NOT_FOUND));
        goalRepository.delete(goal);
    }

    // =========================================================================
    // 4. Evaluations & Reviews
    // =========================================================================


    @Transactional(readOnly = true)
    public PageData<PerformanceEvaluationResponse> getEvaluations(EvaluationFilter filter) {
        Pageable pageable = (filter != null)
                ? filter.toPageable("createdAt", ALLOWED_EVALUATION_SORT_FIELDS)
                : PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"));

        UUID companyId = filter != null ? filter.getCompanyId() : null;
        UUID cycleId = filter != null ? filter.getPerformanceCycleId() : null;
        UUID employeeId = filter != null ? filter.getEmployeeId() : null;
        EvaluationStatus status = filter != null ? filter.getStatus() : null;

        // DataScope Evaluation (Rule 4.3)
        DataScope scope = permEvaluator.getDataScope("performance.view").orElse(DataScope.OWN);
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        UUID currentEmpId = employeeService.findEmployeeIdByUserId(currentUserId).orElse(null);
        Collection<UUID> allowedEmployeeIds = null;

        switch (scope) {
            case OWN -> {
                if (currentEmpId == null) {
                    return PageData.of(Page.empty(pageable), List.of());
                }
                allowedEmployeeIds = Set.of(currentEmpId);
            }
            case TEAM, DEPARTMENT -> {
                if (currentEmpId != null) {
                    allowedEmployeeIds = employeeService.getSubordinateEmployeeIds(currentEmpId);
                } else {
                    return PageData.of(Page.empty(pageable), List.of());
                }
            }
            case COMPANY -> {
                if (companyId == null && currentEmpId != null) {
                    EmployeeDetailResponse currentEmp = employeeService.getEmployeeByIdInternal(currentEmpId);
                    if (currentEmp.getCompany() != null) {
                        companyId = currentEmp.getCompany().getId();
                    }
                }
            }
            case ALL -> {
                // Toàn quyền
            }
        }

        Page<PerformanceEvaluation> page = evaluationRepository.searchEvaluations(companyId, cycleId, employeeId, allowedEmployeeIds, status, pageable);
        if (page.isEmpty()) {
            return PageData.of(page, List.of());
        }

        // Batch Enrichment (Rule 8.3)
        Set<UUID> empIds = page.getContent().stream()
                .map(PerformanceEvaluation::getEmployeeId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<UUID> finalizerIds = page.getContent().stream()
                .map(PerformanceEvaluation::getFinalizedByEmployeeId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<UUID> allEmpIds = new HashSet<>(empIds);
        allEmpIds.addAll(finalizerIds);
        Map<UUID, EmployeeSummary> empSummaryMap = employeeService.getEmployeeSummaries(allEmpIds);

        Set<UUID> cycleIds = page.getContent().stream()
                .map(PerformanceEvaluation::getPerformanceCycleId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<UUID, String> cycleMap = cycleRepository.findAllById(cycleIds).stream()
                .collect(Collectors.toMap(PerformanceCycle::getId, PerformanceCycle::getName));

        List<PerformanceEvaluationResponse> content = page.getContent().stream().map(eval -> {
            PerformanceEvaluationResponse res = performanceMapper.toResponse(eval);
            EmployeeSummary emp = empSummaryMap.get(eval.getEmployeeId());
            res.setEmployee(emp);
            if (emp != null) {
                res.setEmployeeName(emp.getFullName());
                res.setEmployeeCode(emp.getEmployeeCode());
            }
            res.setPerformanceCycleName(cycleMap.get(eval.getPerformanceCycleId()));

            if (eval.getFinalizedByEmployeeId() != null) {
                EmployeeSummary fin = empSummaryMap.get(eval.getFinalizedByEmployeeId());
                res.setFinalizedBy(fin);
                if (fin != null) {
                    res.setFinalizedByEmployeeName(fin.getFullName());
                }
            }
            return res;
        }).toList();

        return PageData.of(page, content);
    }


    @Transactional(readOnly = true)
    public PerformanceEvaluationDetailResponse getEvaluationDetail(UUID evaluationId) {
        PerformanceEvaluation evaluation = evaluationRepository.findById(evaluationId)
                .orElseThrow(() -> new AppException(PerformanceErrorCode.EVALUATION_NOT_FOUND));
        return buildEvaluationDetailResponse(evaluation);
    }


    @Transactional
    public PerformanceEvaluationDetailResponse getMyEvaluation(UUID currentUserId, UUID cycleId) {
        UUID employeeId = employeeService.findEmployeeIdByUserId(currentUserId)
                .orElseThrow(() -> new AppException(PerformanceErrorCode.CURRENT_USER_NOT_LINKED_EMPLOYEE));

        EmployeeDetailResponse empDetail = employeeService.getEmployeeByIdInternal(employeeId);

        UUID targetCycleId = cycleId;
        if (targetCycleId == null) {
            PerformanceCycle activeCycle = cycleRepository.findFirstByStatus(PerformanceCycleStatus.ACTIVE)
                    .orElseThrow(() -> new AppException(PerformanceErrorCode.CYCLE_NOT_FOUND));
            targetCycleId = activeCycle.getId();
        }

        UUID companyId = empDetail.getCompany() != null ? empDetail.getCompany().getId() : null;
        PerformanceEvaluation evaluation = ensureEvaluationExists(employeeId, targetCycleId, companyId);
        return buildEvaluationDetailResponse(evaluation);
    }


    @Transactional
    public PerformanceEvaluationDetailResponse submitSelfReview(UUID evaluationId, SubmitSelfReviewRequest request, UUID currentUserId) {
        PerformanceEvaluation evaluation = evaluationRepository.findById(evaluationId)
                .orElseThrow(() -> new AppException(PerformanceErrorCode.EVALUATION_NOT_FOUND));

        if (evaluation.getStatus() == EvaluationStatus.FINALIZED) {
            throw new AppException(PerformanceErrorCode.EVALUATION_ALREADY_FINALIZED);
        }

        UUID currentEmpId = employeeService.findEmployeeIdByUserId(currentUserId).orElse(null);
        if (currentEmpId != null && !currentEmpId.equals(evaluation.getEmployeeId())) {
            throw new AppException(PerformanceErrorCode.UNAUTHORIZED_ACCESS);
        }

        UUID reviewerId = currentEmpId != null ? currentEmpId : evaluation.getEmployeeId();

        for (GoalReviewItemRequest item : request.getReviews()) {
            Goal goal = goalRepository.findById(item.getGoalId())
                    .orElseThrow(() -> new AppException(PerformanceErrorCode.GOAL_NOT_FOUND));
            if (!goal.getEmployeeId().equals(evaluation.getEmployeeId())) {
                throw new AppException(PerformanceErrorCode.UNAUTHORIZED_ACCESS);
            }

            Optional<PerformanceReview> existing = reviewRepository.findByGoalIdAndReviewType(goal.getId(), ReviewType.SELF);
            PerformanceReview review;
            if (existing.isPresent()) {
                review = existing.get();
                review.setScore(item.getScore());
                review.setComment(item.getComment());
                review.setSubmittedAt(LocalDateTime.now());
            } else {
                review = PerformanceReview.builder()
                        .goalId(goal.getId())
                        .evaluationId(evaluation.getId())
                        .reviewType(ReviewType.SELF)
                        .reviewerEmployeeId(reviewerId)
                        .score(item.getScore())
                        .comment(item.getComment())
                        .submittedAt(LocalDateTime.now())
                        .build();
            }
            reviewRepository.save(review);
        }

        evaluation.setSelfOverallComment(request.getSelfOverallComment());
        evaluation.setSelfSubmittedAt(LocalDateTime.now());
        if (evaluation.getStatus() == EvaluationStatus.DRAFT) {
            evaluation.setStatus(EvaluationStatus.SELF_REVIEWED);
        }
        evaluation = evaluationRepository.save(evaluation);

        return buildEvaluationDetailResponse(evaluation);
    }


    @Transactional
    public PerformanceEvaluationDetailResponse submitManagerReview(UUID evaluationId, SubmitManagerReviewRequest request, UUID currentUserId) {
        PerformanceEvaluation evaluation = evaluationRepository.findById(evaluationId)
                .orElseThrow(() -> new AppException(PerformanceErrorCode.EVALUATION_NOT_FOUND));

        if (evaluation.getStatus() == EvaluationStatus.FINALIZED) {
            throw new AppException(PerformanceErrorCode.EVALUATION_ALREADY_FINALIZED);
        }

        UUID managerId = employeeService.findEmployeeIdByUserId(currentUserId).orElse(null);
        UUID reviewerId = managerId != null ? managerId : evaluation.getEmployeeId();

        for (GoalReviewItemRequest item : request.getReviews()) {
            Goal goal = goalRepository.findById(item.getGoalId())
                    .orElseThrow(() -> new AppException(PerformanceErrorCode.GOAL_NOT_FOUND));
            if (!goal.getEmployeeId().equals(evaluation.getEmployeeId())) {
                throw new AppException(PerformanceErrorCode.UNAUTHORIZED_ACCESS);
            }

            Optional<PerformanceReview> existing = reviewRepository.findByGoalIdAndReviewType(goal.getId(), ReviewType.MANAGER);
            PerformanceReview review;
            if (existing.isPresent()) {
                review = existing.get();
                review.setScore(item.getScore());
                review.setComment(item.getComment());
                review.setSubmittedAt(LocalDateTime.now());
            } else {
                review = PerformanceReview.builder()
                        .goalId(goal.getId())
                        .evaluationId(evaluation.getId())
                        .reviewType(ReviewType.MANAGER)
                        .reviewerEmployeeId(reviewerId)
                        .score(item.getScore())
                        .comment(item.getComment())
                        .submittedAt(LocalDateTime.now())
                        .build();
            }
            reviewRepository.save(review);
        }

        evaluation.setManagerOverallComment(request.getManagerOverallComment());
        evaluation.setManagerSubmittedAt(LocalDateTime.now());
        evaluation.setStatus(EvaluationStatus.MANAGER_REVIEWED);

        // Tính điểm trung bình có trọng số theo đánh giá của Quản lý
        List<Goal> goals = goalRepository.findByEmployeeIdAndPerformanceCycleId(evaluation.getEmployeeId(), evaluation.getPerformanceCycleId());
        BigDecimal totalScore = BigDecimal.ZERO;
        BigDecimal totalWeight = BigDecimal.ZERO;

        for (Goal g : goals) {
            Optional<PerformanceReview> mr = reviewRepository.findByGoalIdAndReviewType(g.getId(), ReviewType.MANAGER);
            if (mr.isPresent() && mr.get().getScore() != null) {
                BigDecimal weight = g.getWeightPercentage() != null ? g.getWeightPercentage() : BigDecimal.ZERO;
                totalScore = totalScore.add(mr.get().getScore().multiply(weight));
                totalWeight = totalWeight.add(weight);
            }
        }

        if (totalWeight.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal finalScore = totalScore.divide(totalWeight, 2, RoundingMode.HALF_UP);
            evaluation.setFinalScore(finalScore);
            evaluation.setRating(determineRating(finalScore));
        }

        evaluation = evaluationRepository.save(evaluation);
        return buildEvaluationDetailResponse(evaluation);
    }


    @Transactional
    public PerformanceEvaluationDetailResponse finalizeEvaluation(UUID evaluationId, FinalizeEvaluationRequest request, UUID currentUserId) {
        PerformanceEvaluation evaluation = evaluationRepository.findById(evaluationId)
                .orElseThrow(() -> new AppException(PerformanceErrorCode.EVALUATION_NOT_FOUND));

        if (evaluation.getStatus() == EvaluationStatus.FINALIZED) {
            throw new AppException(PerformanceErrorCode.EVALUATION_ALREADY_FINALIZED);
        }

        UUID finalizerEmpId = employeeService.findEmployeeIdByUserId(currentUserId).orElse(null);

        evaluation.setFinalScore(request.getFinalScore());
        evaluation.setRating(request.getRating() != null ? request.getRating() : determineRating(request.getFinalScore()));
        evaluation.setStatus(EvaluationStatus.FINALIZED);
        evaluation.setFinalizedByEmployeeId(finalizerEmpId);
        evaluation.setFinalizedAt(LocalDateTime.now());

        evaluation = evaluationRepository.save(evaluation);
        return buildEvaluationDetailResponse(evaluation);
    }

    // =========================================================================
    // Helper Methods & Detail Builders
    // =========================================================================

    private PerformanceEvaluation ensureEvaluationExists(UUID employeeId, UUID cycleId, UUID companyId) {
        return evaluationRepository.findByEmployeeIdAndPerformanceCycleId(employeeId, cycleId)
                .orElseGet(() -> {
                    PerformanceEvaluation newEval = PerformanceEvaluation.builder()
                            .employeeId(employeeId)
                            .performanceCycleId(cycleId)
                            .companyId(companyId)
                            .status(EvaluationStatus.DRAFT)
                            .build();
                    return evaluationRepository.save(newEval);
                });
    }

    private PerformanceEvaluationDetailResponse buildEvaluationDetailResponse(PerformanceEvaluation eval) {
        EmployeeDetailResponse empDetail = null;
        try {
            empDetail = employeeService.getEmployeeByIdInternal(eval.getEmployeeId());
        } catch (Exception e) {
            log.warn("Could not find employee detail for employeeId={}", eval.getEmployeeId());
        }

        EmployeeSummary empSummary = (empDetail != null)
                ? EmployeeSummary.builder()
                .id(empDetail.getId())
                .employeeCode(empDetail.getEmployeeCode())
                .fullName(empDetail.getFullName())
                .photoUrl(empDetail.getPhotoUrl())
                .companyEmail(empDetail.getCompanyEmail())
                .build()
                : employeeService.getEmployeeSummary(eval.getEmployeeId());

        OrgUnitSummary dept = empDetail != null ? empDetail.getOrganizationalUnit() : null;
        PositionSummary pos = empDetail != null ? empDetail.getPosition() : null;

        String cycleName = cycleRepository.findById(eval.getPerformanceCycleId())
                .map(PerformanceCycle::getName)
                .orElse(null);

        EmployeeSummary finalizedBy = (eval.getFinalizedByEmployeeId() != null)
                ? employeeService.getEmployeeSummary(eval.getFinalizedByEmployeeId())
                : null;

        // Lấy danh sách goals và reviews kèm theo
        List<Goal> goals = goalRepository.findByEmployeeIdAndPerformanceCycleId(eval.getEmployeeId(), eval.getPerformanceCycleId());
        Set<UUID> kpiIds = goals.stream().map(Goal::getKpiId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<UUID, Kpi> kpiMap = kpiRepository.findAllById(kpiIds).stream()
                .collect(Collectors.toMap(Kpi::getId, Function.identity()));

        List<GoalDetailResponse> goalDetails = goals.stream().map(g -> {
            PerformanceReview selfReview = reviewRepository.findByGoalIdAndReviewType(g.getId(), ReviewType.SELF).orElse(null);
            PerformanceReview managerReview = reviewRepository.findByGoalIdAndReviewType(g.getId(), ReviewType.MANAGER).orElse(null);

            Kpi k = g.getKpiId() != null ? kpiMap.get(g.getKpiId()) : null;

            return GoalDetailResponse.builder()
                    .id(g.getId())
                    .employeeId(g.getEmployeeId())
                    .performanceCycleId(g.getPerformanceCycleId())
                    .kpiId(g.getKpiId())
                    .kpiName(k != null ? k.getName() : null)
                    .kpiUnit(k != null ? k.getUnit() : null)
                    .title(g.getTitle())
                    .targetValue(g.getTargetValue())
                    .weightPercentage(g.getWeightPercentage())
                    .status(g.getStatus())
                    .description(g.getDescription())
                    .selfReview(selfReview != null ? performanceMapper.toResponse(selfReview) : null)
                    .managerReview(managerReview != null ? performanceMapper.toResponse(managerReview) : null)
                    .build();
        }).toList();

        return PerformanceEvaluationDetailResponse.builder()
                .id(eval.getId())
                .employeeId(eval.getEmployeeId())
                .employeeName(empSummary != null ? empSummary.getFullName() : null)
                .employeeCode(empSummary != null ? empSummary.getEmployeeCode() : null)
                .employee(empSummary)
                .departmentName(dept != null ? dept.getName() : null)
                .department(dept)
                .positionName(pos != null ? pos.getName() : null)
                .position(pos)
                .performanceCycleId(eval.getPerformanceCycleId())
                .performanceCycleName(cycleName)
                .status(eval.getStatus())
                .finalScore(eval.getFinalScore())
                .rating(eval.getRating())
                .selfOverallComment(eval.getSelfOverallComment())
                .managerOverallComment(eval.getManagerOverallComment())
                .selfSubmittedAt(eval.getSelfSubmittedAt())
                .managerSubmittedAt(eval.getManagerSubmittedAt())
                .finalizedByEmployeeId(eval.getFinalizedByEmployeeId())
                .finalizedByEmployeeName(finalizedBy != null ? finalizedBy.getFullName() : null)
                .finalizedBy(finalizedBy)
                .finalizedAt(eval.getFinalizedAt())
                .goals(goalDetails)
                .build();
    }

    private PerformanceRating determineRating(BigDecimal score) {
        if (score == null) return null;
        double s = score.doubleValue();
        if (s >= 90.0) return PerformanceRating.EXCELLENT;
        if (s >= 80.0) return PerformanceRating.GOOD;
        if (s >= 65.0) return PerformanceRating.SATISFACTORY;
        if (s >= 50.0) return PerformanceRating.NEEDS_IMPROVEMENT;
        return PerformanceRating.POOR;
    }
}
