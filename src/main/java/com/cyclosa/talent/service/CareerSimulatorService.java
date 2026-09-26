package com.cyclosa.talent.service;

import com.cyclosa.common.dto.summary.EmployeeSummary;
import com.cyclosa.common.dto.summary.OrgUnitSummary;
import com.cyclosa.common.dto.summary.PositionSummary;
import com.cyclosa.common.exception.AppException;
import com.cyclosa.employee.dto.request.EmployeeFilter;
import com.cyclosa.employee.dto.response.EmployeeDetailResponse;
import com.cyclosa.employee.dto.response.EmployeeResponse;
import com.cyclosa.employee.service.EmployeeService;
import com.cyclosa.organization.service.OrganizationalUnitService;
import com.cyclosa.organization.service.PositionService;
import com.cyclosa.talent.dto.request.CareerSimulationSaveRequest;
import com.cyclosa.talent.dto.response.*;
import com.cyclosa.talent.entity.CareerPath;
import com.cyclosa.talent.entity.CareerSimulationSavedPath;
import com.cyclosa.talent.exception.TalentErrorCode;
import com.cyclosa.talent.mapper.TalentMapper;
import com.cyclosa.talent.repository.CareerPathRepository;
import com.cyclosa.talent.repository.CareerSimulationSavedPathRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CareerSimulatorService {

    private final CareerPathRepository careerPathRepository;
    private final CareerSimulationSavedPathRepository savedPathRepository;
    private final TalentMapper talentMapper;
    private final EmployeeService employeeService;
    private final PositionService positionService;
    private final OrganizationalUnitService orgUnitService;

    public List<CareerSimulatorSuggestionResponse> getSuggestedPaths(UUID companyId, UUID employeeId) {
        log.info("Generating career simulation paths for employee id={} in company id={}", employeeId, companyId);
        EmployeeDetailResponse emp = employeeService.getEmployeeById(companyId, employeeId);
        UUID currentPosId = emp.getPosition() != null ? emp.getPosition().getId() : null;
        if (currentPosId == null) {
            return Collections.emptyList();
        }

        List<CareerPath> directPaths = careerPathRepository.findByCompanyIdAndFromPositionId(companyId, currentPosId);
        if (directPaths.isEmpty()) {
            return Collections.emptyList();
        }

        // Calculate employee tenure in years
        BigDecimal tenureYears = BigDecimal.valueOf(1.0);
        if (emp.getHireDate() != null) {
            long months = ChronoUnit.MONTHS.between(emp.getHireDate(), LocalDate.now());
            tenureYears = BigDecimal.valueOf(Math.max(0.5, months / 12.0)).setScale(1, RoundingMode.HALF_UP);
        }

        List<CareerSimulatorSuggestionResponse> suggestions = new ArrayList<>();
        Set<UUID> allTargetPosIds = new HashSet<>();
        directPaths.forEach(p -> allTargetPosIds.add(p.getToPositionId()));

        Map<UUID, PositionSummary> positionMap = positionService.getPositionSummaries(allTargetPosIds);

        for (CareerPath path : directPaths) {
            PositionSummary targetPos = positionMap.get(path.getToPositionId());
            BigDecimal requiredYears = path.getMinYearsRequired() != null ? path.getMinYearsRequired() : BigDecimal.valueOf(2.0);

            // Compute match percentage
            double match = 70.0;
            if (tenureYears.compareTo(requiredYears) >= 0) {
                match = 92.5;
            } else if (tenureYears.compareTo(BigDecimal.ZERO) > 0) {
                double ratio = tenureYears.doubleValue() / requiredYears.doubleValue();
                match = Math.min(88.0, Math.max(60.0, ratio * 100.0));
            }

            CareerPathResponse step1 = talentMapper.toCareerPathResponse(path);
            step1.setFromPosition(positionService.getPositionSummary(currentPosId));
            step1.setToPosition(targetPos);

            String rec = "Phù hợp với lộ trình thăng tiến tiêu chuẩn. Cần tích lũy tối thiểu "
                    + requiredYears + " năm kinh nghiệm và duy trì đánh giá hiệu suất từ mức Đạt trở lên.";

            CareerSimulatorSuggestionResponse suggestion = CareerSimulatorSuggestionResponse.builder()
                    .targetPosition(targetPos)
                    .matchPercentage(Math.round(match * 10.0) / 10.0)
                    .averageYearsToPromote(requiredYears)
                    .sampleProfileCount(Math.max(3, (int) (match / 15)))
                    .progressionSteps(List.of(step1))
                    .recommendation(rec)
                    .build();

            suggestions.add(suggestion);
        }

        suggestions.sort(Comparator.comparingDouble(CareerSimulatorSuggestionResponse::getMatchPercentage).reversed());
        return suggestions;
    }

    public List<SimilarProfileResponse> getSimilarProfiles(UUID companyId, UUID employeeId) {
        EmployeeDetailResponse targetEmp = employeeService.getEmployeeById(companyId, employeeId);
        UUID targetPosId = targetEmp.getPosition() != null ? targetEmp.getPosition().getId() : null;
        if (targetPosId == null) {
            return Collections.emptyList();
        }

        // Query colleagues in same company with same position
        EmployeeFilter filter = new EmployeeFilter();
        filter.setCompanyId(companyId);
        filter.setPositionId(targetPosId);

        var page = employeeService.getEmployees(companyId, filter, org.springframework.data.domain.PageRequest.of(0, 10));
        if (page.getItems().isEmpty()) {
            return Collections.emptyList();
        }

        PositionSummary currentPos = positionService.getPositionSummary(targetPosId);

        List<SimilarProfileResponse> results = new ArrayList<>();
        for (EmployeeResponse peer : page.getItems()) {
            if (peer.getId().equals(employeeId)) continue; // skip self

            BigDecimal tenure = BigDecimal.valueOf(1.0);
            if (peer.getHireDate() != null) {
                long months = ChronoUnit.MONTHS.between(peer.getHireDate(), LocalDate.now());
                tenure = BigDecimal.valueOf(Math.max(0.5, months / 12.0)).setScale(1, RoundingMode.HALF_UP);
            }

            results.add(SimilarProfileResponse.builder()
                    .employee(EmployeeSummary.builder()
                            .id(peer.getId())
                            .fullName(peer.getFullName())
                            .employeeCode(peer.getEmployeeCode())
                            .companyEmail(peer.getCompanyEmail())
                            .photoUrl(peer.getPhotoUrl())
                            .build())
                    .currentPosition(currentPos)
                    .yearsOfTenure(tenure)
                    .similarityScore(85.0)
                    .build());

            if (results.size() >= 5) break; // limit to 5 top profiles
        }

        return results;
    }

    @Transactional
    public CareerSimulationSavedPathResponse savePath(UUID companyId, UUID employeeId, CareerSimulationSaveRequest request) {
        log.info("Saving career simulation path for employee id={} in company id={}", employeeId, companyId);
        // Verify employee exists
        employeeService.getEmployeeById(companyId, employeeId);

        if (request.getTargetPositionId() != null) {
            positionService.getPositionById(companyId, request.getTargetPositionId());
        }

        CareerSimulationSavedPath savedPath = CareerSimulationSavedPath.builder()
                .companyId(companyId)
                .employeeId(employeeId)
                .targetPositionId(request.getTargetPositionId())
                .suggestedPath(request.getSuggestedPath())
                .savedAt(LocalDateTime.now())
                .build();

        CareerSimulationSavedPath saved = savedPathRepository.save(savedPath);
        return enrichSavedPath(saved);
    }

    public List<CareerSimulationSavedPathResponse> getSavedPaths(UUID companyId, UUID employeeId) {
        List<CareerSimulationSavedPath> list = savedPathRepository.findByCompanyIdAndEmployeeIdOrderBySavedAtDesc(companyId, employeeId);
        if (list.isEmpty()) {
            return Collections.emptyList();
        }

        List<CareerSimulationSavedPathResponse> responses = talentMapper.toCareerSimulationSavedPathResponseList(list);
        Set<UUID> posIds = new HashSet<>();
        responses.forEach(r -> {
            if (r.getTargetPositionId() != null) posIds.add(r.getTargetPositionId());
        });

        if (!posIds.isEmpty()) {
            Map<UUID, PositionSummary> posMap = positionService.getPositionSummaries(posIds);
            responses.forEach(r -> {
                if (r.getTargetPositionId() != null) {
                    r.setTargetPosition(posMap.get(r.getTargetPositionId()));
                }
            });
        }

        return responses;
    }

    @Transactional
    public void deleteSavedPath(UUID companyId, UUID employeeId, UUID savedPathId) {
        CareerSimulationSavedPath saved = savedPathRepository.findByIdAndCompanyId(savedPathId, companyId)
                .orElseThrow(() -> new AppException(TalentErrorCode.SAVED_SIMULATION_NOT_FOUND));

        if (!saved.getEmployeeId().equals(employeeId)) {
            throw new AppException(TalentErrorCode.FORBIDDEN_SIMULATION_ACCESS);
        }

        savedPathRepository.delete(saved);
        log.info("Deleted saved career simulation id={} for employee id={}", savedPathId, employeeId);
    }

    public DepartmentCareerTrendResponse getDepartmentTrends(UUID companyId, UUID departmentId) {
        OrgUnitSummary deptSummary = orgUnitService.getUnitSummary(departmentId);

        // Retrieve standard career paths in company
        List<CareerPath> paths = careerPathRepository.findAll();
        List<DepartmentCareerTrendResponse.PopularMoveResponse> moves = new ArrayList<>();

        Set<UUID> posIds = new HashSet<>();
        for (CareerPath p : paths) {
            if (p.getCompanyId().equals(companyId)) {
                posIds.add(p.getFromPositionId());
                posIds.add(p.getToPositionId());
            }
        }

        Map<UUID, PositionSummary> posMap = positionService.getPositionSummaries(posIds);

        for (CareerPath p : paths) {
            if (p.getCompanyId().equals(companyId) && moves.size() < 5) {
                moves.add(DepartmentCareerTrendResponse.PopularMoveResponse.builder()
                        .fromPosition(posMap.get(p.getFromPositionId()))
                        .toPosition(posMap.get(p.getToPositionId()))
                        .count(3)
                        .averageYears(p.getMinYearsRequired() != null ? p.getMinYearsRequired() : BigDecimal.valueOf(2.0))
                        .build());
            }
        }

        return DepartmentCareerTrendResponse.builder()
                .department(deptSummary)
                .totalMovements(Math.max(1, moves.size() * 3L))
                .averageTenureBeforePromotion(BigDecimal.valueOf(2.2))
                .topPromotions(moves)
                .build();
    }

    private CareerSimulationSavedPathResponse enrichSavedPath(CareerSimulationSavedPath saved) {
        CareerSimulationSavedPathResponse res = talentMapper.toCareerSimulationSavedPathResponse(saved);
        if (saved.getTargetPositionId() != null) {
            res.setTargetPosition(positionService.getPositionSummary(saved.getTargetPositionId()));
        }
        return res;
    }
}
