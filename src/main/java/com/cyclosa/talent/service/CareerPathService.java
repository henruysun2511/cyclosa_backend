package com.cyclosa.talent.service;

import com.cyclosa.common.dto.summary.PositionSummary;
import com.cyclosa.common.exception.AppException;
import com.cyclosa.common.response.PageData;
import com.cyclosa.employee.dto.response.EmployeeDetailResponse;
import com.cyclosa.employee.service.EmployeeService;
import com.cyclosa.organization.service.PositionService;
import com.cyclosa.talent.dto.filter.CareerPathFilter;
import com.cyclosa.talent.dto.request.CreateCareerPathRequest;
import com.cyclosa.talent.dto.request.UpdateCareerPathRequest;
import com.cyclosa.talent.dto.response.CareerPathResponse;
import com.cyclosa.talent.entity.CareerPath;
import com.cyclosa.talent.exception.TalentErrorCode;
import com.cyclosa.talent.mapper.TalentMapper;
import com.cyclosa.talent.repository.CareerPathRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CareerPathService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("createdAt", "minYearsRequired");

    private final CareerPathRepository careerPathRepository;
    private final TalentMapper talentMapper;
    private final PositionService positionService;
    private final EmployeeService employeeService;

    @Transactional
    public CareerPathResponse createCareerPath(UUID companyId, CreateCareerPathRequest request) {
        log.info("Creating career path in company id={} from position={} to position={}",
                companyId, request.getFromPositionId(), request.getToPositionId());

        if (request.getFromPositionId().equals(request.getToPositionId())) {
            throw new AppException(TalentErrorCode.SAME_FROM_TO_POSITION);
        }

        // Validate positions exist via public service
        positionService.getPositionById(companyId, request.getFromPositionId());
        positionService.getPositionById(companyId, request.getToPositionId());

        if (careerPathRepository.existsByCompanyIdAndFromPositionIdAndToPositionId(
                companyId, request.getFromPositionId(), request.getToPositionId())) {
            throw new AppException(TalentErrorCode.CAREER_PATH_ALREADY_EXISTS);
        }

        CareerPath careerPath = CareerPath.builder()
                .companyId(companyId)
                .fromPositionId(request.getFromPositionId())
                .toPositionId(request.getToPositionId())
                .description(request.getDescription())
                .minYearsRequired(request.getMinYearsRequired())
                .build();

        CareerPath saved = careerPathRepository.save(careerPath);
        return enrichCareerPath(saved);
    }

    public PageData<CareerPathResponse> getCareerPaths(UUID companyId, CareerPathFilter filter) {
        Specification<CareerPath> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("companyId"), companyId));

            if (filter.getFromPositionId() != null) {
                predicates.add(cb.equal(root.get("fromPositionId"), filter.getFromPositionId()));
            }
            if (filter.getToPositionId() != null) {
                predicates.add(cb.equal(root.get("toPositionId"), filter.getToPositionId()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Pageable pageable = filter.toPageable("createdAt", ALLOWED_SORT_FIELDS);
        Page<CareerPath> page = careerPathRepository.findAll(spec, pageable);

        if (page.isEmpty()) {
            return PageData.empty(pageable);
        }

        List<CareerPathResponse> responses = talentMapper.toCareerPathResponseList(page.getContent());
        enrichPositions(responses);
        return PageData.of(page, responses);
    }

    public CareerPathResponse getCareerPathById(UUID companyId, UUID id) {
        CareerPath careerPath = careerPathRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new AppException(TalentErrorCode.CAREER_PATH_NOT_FOUND));
        return enrichCareerPath(careerPath);
    }

    @Transactional
    public CareerPathResponse updateCareerPath(UUID companyId, UUID id, UpdateCareerPathRequest request) {
        CareerPath careerPath = careerPathRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new AppException(TalentErrorCode.CAREER_PATH_NOT_FOUND));

        if (request.getDescription() != null) {
            careerPath.setDescription(request.getDescription());
        }
        if (request.getMinYearsRequired() != null) {
            careerPath.setMinYearsRequired(request.getMinYearsRequired());
        }

        CareerPath saved = careerPathRepository.save(careerPath);
        return enrichCareerPath(saved);
    }

    @Transactional
    public void deleteCareerPath(UUID companyId, UUID id) {
        CareerPath careerPath = careerPathRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new AppException(TalentErrorCode.CAREER_PATH_NOT_FOUND));
        careerPathRepository.delete(careerPath);
        log.info("Deleted career path id={} in company id={}", id, companyId);
    }

    public List<CareerPathResponse> getCareerPathsForEmployee(UUID companyId, UUID employeeId) {
        EmployeeDetailResponse emp = employeeService.getEmployeeById(companyId, employeeId);
        UUID positionId = emp.getPosition() != null ? emp.getPosition().getId() : null;
        if (positionId == null) {
            return Collections.emptyList();
        }

        List<CareerPath> paths = careerPathRepository.findByCompanyIdAndFromPositionId(companyId, positionId);
        if (paths.isEmpty()) {
            return Collections.emptyList();
        }

        List<CareerPathResponse> responses = talentMapper.toCareerPathResponseList(paths);
        enrichPositions(responses);
        return responses;
    }

    private CareerPathResponse enrichCareerPath(CareerPath careerPath) {
        CareerPathResponse res = talentMapper.toCareerPathResponse(careerPath);
        enrichPositions(List.of(res));
        return res;
    }

    private void enrichPositions(List<CareerPathResponse> responses) {
        Set<UUID> positionIds = new HashSet<>();
        for (CareerPathResponse res : responses) {
            if (res.getFromPositionId() != null) positionIds.add(res.getFromPositionId());
            if (res.getToPositionId() != null) positionIds.add(res.getToPositionId());
        }

        if (positionIds.isEmpty()) return;

        Map<UUID, PositionSummary> summaryMap = positionService.getPositionSummaries(positionIds);
        for (CareerPathResponse res : responses) {
            res.setFromPosition(summaryMap.get(res.getFromPositionId()));
            res.setToPosition(summaryMap.get(res.getToPositionId()));
        }
    }
}
