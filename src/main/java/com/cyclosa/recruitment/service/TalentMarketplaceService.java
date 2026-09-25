package com.cyclosa.recruitment.service;

import com.cyclosa.common.dto.summary.EmployeeSummary;
import com.cyclosa.common.dto.summary.OrgUnitSummary;
import com.cyclosa.common.exception.AppException;
import com.cyclosa.common.response.PageData;
import com.cyclosa.employee.dto.response.EmployeeDetailResponse;
import com.cyclosa.employee.service.EmployeeService;
import com.cyclosa.notification.enums.NotificationType;
import com.cyclosa.notification.service.NotificationService;
import com.cyclosa.organization.service.OrganizationalUnitService;
import com.cyclosa.recruitment.dto.request.*;
import com.cyclosa.recruitment.dto.response.*;
import com.cyclosa.recruitment.entity.InternalApplication;
import com.cyclosa.recruitment.entity.InternalAssignment;
import com.cyclosa.recruitment.entity.InternalOpportunity;
import com.cyclosa.recruitment.enums.AssignmentStatus;
import com.cyclosa.recruitment.enums.InternalApplicationStatus;
import com.cyclosa.recruitment.enums.OpportunityStatus;
import com.cyclosa.recruitment.exception.RecruitmentErrorCode;
import com.cyclosa.recruitment.repository.InternalApplicationRepository;
import com.cyclosa.recruitment.repository.InternalAssignmentRepository;
import com.cyclosa.recruitment.repository.InternalOpportunityRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TalentMarketplaceService {

    private final InternalOpportunityRepository opportunityRepository;
    private final InternalApplicationRepository applicationRepository;
    private final InternalAssignmentRepository assignmentRepository;
    private final EmployeeService employeeService;
    private final OrganizationalUnitService orgUnitService;
    private final NotificationService notificationService;

    // =========================================================================
    // 1. OPPORTUNITIES
    // =========================================================================

    @Transactional(readOnly = true)
    public PageData<InternalOpportunityResponse> getOpportunities(UUID companyId, OpportunityFilter filter, Pageable pageable) {
        Specification<InternalOpportunity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("companyId"), companyId));

            if (filter != null) {
                if (filter.getType() != null) {
                    predicates.add(cb.equal(root.get("type"), filter.getType()));
                }
                if (filter.getStatus() != null) {
                    predicates.add(cb.equal(root.get("status"), filter.getStatus()));
                }
                if (filter.getDepartmentId() != null) {
                    predicates.add(cb.equal(root.get("departmentId"), filter.getDepartmentId()));
                }
                if (filter.getKeyword() != null && !filter.getKeyword().isBlank()) {
                    String kw = "%" + filter.getKeyword().trim().toLowerCase() + "%";
                    predicates.add(cb.or(
                            cb.like(cb.lower(root.get("title")), kw),
                            cb.like(cb.lower(root.get("requiredSkills")), kw)
                    ));
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<InternalOpportunity> page = opportunityRepository.findAll(spec, pageable);
        List<InternalOpportunityResponse> content = enrichOpportunities(companyId, page.getContent());
        return PageData.of(page, content);
    }

    @Transactional(readOnly = true)
    public InternalOpportunityResponse getOpportunityById(UUID companyId, UUID id) {
        InternalOpportunity entity = opportunityRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new AppException(RecruitmentErrorCode.OPPORTUNITY_NOT_FOUND));
        return enrichOpportunities(companyId, List.of(entity)).get(0);
    }

    @Transactional
    public InternalOpportunityResponse createOpportunity(UUID companyId, CreateOpportunityRequest request) {
        InternalOpportunity entity = InternalOpportunity.builder()
                .companyId(companyId)
                .title(request.getTitle())
                .type(request.getType())
                .departmentId(request.getDepartmentId())
                .managerId(request.getManagerId())
                .description(request.getDescription())
                .requiredSkills(request.getRequiredSkills())
                .commitmentPercentage(request.getCommitmentPercentage() != null ? request.getCommitmentPercentage() : 100)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(OpportunityStatus.OPEN)
                .build();

        entity = opportunityRepository.save(entity);
        log.info("Created internal opportunity id={}, title={}", entity.getId(), entity.getTitle());
        return enrichOpportunities(companyId, List.of(entity)).get(0);
    }

    @Transactional
    public InternalOpportunityResponse updateOpportunity(UUID companyId, UUID id, UpdateOpportunityRequest request) {
        InternalOpportunity entity = opportunityRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new AppException(RecruitmentErrorCode.OPPORTUNITY_NOT_FOUND));

        if (request.getTitle() != null) entity.setTitle(request.getTitle());
        if (request.getType() != null) entity.setType(request.getType());
        if (request.getDepartmentId() != null) entity.setDepartmentId(request.getDepartmentId());
        if (request.getManagerId() != null) entity.setManagerId(request.getManagerId());
        if (request.getDescription() != null) entity.setDescription(request.getDescription());
        if (request.getRequiredSkills() != null) entity.setRequiredSkills(request.getRequiredSkills());
        if (request.getCommitmentPercentage() != null) entity.setCommitmentPercentage(request.getCommitmentPercentage());
        if (request.getStartDate() != null) entity.setStartDate(request.getStartDate());
        if (request.getEndDate() != null) entity.setEndDate(request.getEndDate());
        if (request.getStatus() != null) entity.setStatus(request.getStatus());

        entity = opportunityRepository.save(entity);
        log.info("Updated internal opportunity id={}", id);
        return enrichOpportunities(companyId, List.of(entity)).get(0);
    }

    @Transactional
    public void closeOpportunity(UUID companyId, UUID id) {
        InternalOpportunity entity = opportunityRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new AppException(RecruitmentErrorCode.OPPORTUNITY_NOT_FOUND));

        entity.setStatus(OpportunityStatus.CLOSED);
        opportunityRepository.save(entity);
        log.info("Closed internal opportunity id={}", id);
    }

    @Transactional
    public InternalApplicationResponse expressInterest(UUID companyId, UUID opportunityId, UUID employeeId, ExpressInterestRequest request) {
        InternalOpportunity opportunity = opportunityRepository.findByIdAndCompanyId(opportunityId, companyId)
                .orElseThrow(() -> new AppException(RecruitmentErrorCode.OPPORTUNITY_NOT_FOUND));

        if (opportunity.getStatus() != OpportunityStatus.OPEN) {
            throw new AppException(RecruitmentErrorCode.OPPORTUNITY_INACTIVE);
        }

        if (applicationRepository.findByOpportunityIdAndEmployeeId(opportunityId, employeeId).isPresent()) {
            throw new AppException(RecruitmentErrorCode.INTERNAL_APPLICATION_EXISTS);
        }

        InternalApplication application = InternalApplication.builder()
                .companyId(companyId)
                .opportunityId(opportunityId)
                .employeeId(employeeId)
                .note(request != null ? request.getNote() : null)
                .status(InternalApplicationStatus.APPLIED)
                .build();

        application = applicationRepository.save(application);
        log.info("Employee id={} expressed interest in opportunity id={}", employeeId, opportunityId);

        // Notify opportunity manager if exists
        try {
            if (opportunity.getManagerId() != null) {
                EmployeeDetailResponse mgr = employeeService.getEmployeeByIdInternal(opportunity.getManagerId());
                if (mgr != null && mgr.getUserId() != null) {
                    notificationService.send(
                            mgr.getUserId(),
                            "Ứng viên nội bộ mới",
                            "Có nhân viên vừa bày tỏ quan tâm vào cơ hội: " + opportunity.getTitle(),
                            NotificationType.SYSTEM,
                            "/recruitment/internal-opportunities/" + opportunityId
                    );
                }
            }
        } catch (Exception e) {
            log.warn("Failed to send notification for express interest: {}", e.getMessage());
        }

        return toApplicationResponse(companyId, application, opportunity.getTitle());
    }

    @Transactional(readOnly = true)
    public List<InternalApplicationResponse> getApplicationsByOpportunity(UUID companyId, UUID opportunityId) {
        InternalOpportunity opportunity = opportunityRepository.findByIdAndCompanyId(opportunityId, companyId)
                .orElseThrow(() -> new AppException(RecruitmentErrorCode.OPPORTUNITY_NOT_FOUND));

        List<InternalApplication> list = applicationRepository.findByOpportunityId(opportunityId);
        return list.stream()
                .map(app -> toApplicationResponse(companyId, app, opportunity.getTitle()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InternalApplicationResponse> getMyApplications(UUID companyId, UUID employeeId) {
        List<InternalApplication> list = applicationRepository.findByCompanyIdAndEmployeeId(companyId, employeeId);
        if (list.isEmpty()) return Collections.emptyList();

        Set<UUID> oppIds = list.stream().map(InternalApplication::getOpportunityId).collect(Collectors.toSet());
        Map<UUID, String> oppTitles = opportunityRepository.findAllById(oppIds).stream()
                .collect(Collectors.toMap(InternalOpportunity::getId, InternalOpportunity::getTitle));

        return list.stream()
                .map(app -> toApplicationResponse(companyId, app, oppTitles.getOrDefault(app.getOpportunityId(), "Unknown Opportunity")))
                .collect(Collectors.toList());
    }

    @Transactional
    public InternalApplicationResponse reviewApplication(UUID companyId, UUID applicationId, ReviewInternalApplicationRequest request) {
        InternalApplication application = applicationRepository.findByIdAndCompanyId(applicationId, companyId)
                .orElseThrow(() -> new AppException(RecruitmentErrorCode.INTERNAL_APPLICATION_NOT_FOUND));

        application.setStatus(request.getStatus());
        if (request.getFeedback() != null) {
            application.setFeedback(request.getFeedback());
        }

        final InternalApplication savedApp = applicationRepository.save(application);
        final String status = request.getStatus().name();

        // Notify employee
        try {
            EmployeeDetailResponse emp = employeeService.getEmployeeByIdInternal(savedApp.getEmployeeId());
            if (emp != null && emp.getUserId() != null) {
                String title = opportunityRepository.findById(savedApp.getOpportunityId())
                        .map(InternalOpportunity::getTitle).orElse("Cơ hội nội bộ");
                notificationService.send(
                        emp.getUserId(),
                        "Kết quả xét duyệt ứng tuyển nội bộ",
                        "Đơn bày tỏ quan tâm của bạn cho cơ hội \"" + title + "\" đã được cập nhật thành: " + status,
                        NotificationType.SYSTEM,
                        "/recruitment/my-applications"
                );
            }
        } catch (Exception e) {
            log.warn("Failed to send notification for reviewed application: {}", e.getMessage());
        }

        String title = opportunityRepository.findById(savedApp.getOpportunityId())
                .map(InternalOpportunity::getTitle).orElse("Unknown Opportunity");
        return toApplicationResponse(companyId, savedApp, title);
    }

    @Transactional(readOnly = true)
    public List<InternalOpportunityResponse> getRecommendedOpportunities(UUID companyId, UUID employeeId) {
        List<InternalOpportunity> openList = opportunityRepository.findByCompanyIdAndStatus(companyId, OpportunityStatus.OPEN);
        return enrichOpportunities(companyId, openList);
    }

    @Transactional
    public InternalAssignmentResponse createAssignment(UUID companyId, CreateAssignmentRequest request) {
        InternalOpportunity opportunity = opportunityRepository.findByIdAndCompanyId(request.getOpportunityId(), companyId)
                .orElseThrow(() -> new AppException(RecruitmentErrorCode.OPPORTUNITY_NOT_FOUND));

        InternalAssignment assignment = InternalAssignment.builder()
                .companyId(companyId)
                .opportunityId(request.getOpportunityId())
                .employeeId(request.getEmployeeId())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(AssignmentStatus.ACTIVE)
                .build();

        assignment = assignmentRepository.save(assignment);
        log.info("Created internal assignment id={} for employee id={}, opp id={}",
                assignment.getId(), request.getEmployeeId(), request.getOpportunityId());

        // Update opportunity status if filled
        long activeCount = assignmentRepository.countByCompanyIdAndStatus(companyId, AssignmentStatus.ACTIVE);
        if (activeCount > 0 && opportunity.getType() == com.cyclosa.recruitment.enums.OpportunityType.PROJECT) {
            opportunity.setStatus(OpportunityStatus.FILLED);
            opportunityRepository.save(opportunity);
        }

        return toAssignmentResponse(companyId, assignment, opportunity.getTitle());
    }

    @Transactional(readOnly = true)
    public List<InternalAssignmentResponse> getAssignments(UUID companyId, UUID employeeId, UUID opportunityId) {
        List<InternalAssignment> list;
        if (employeeId != null) {
            list = assignmentRepository.findByCompanyIdAndEmployeeId(companyId, employeeId);
        } else if (opportunityId != null) {
            list = assignmentRepository.findByOpportunityId(opportunityId);
        } else {
            list = assignmentRepository.findAll().stream()
                    .filter(a -> companyId.equals(a.getCompanyId()))
                    .collect(Collectors.toList());
        }

        Set<UUID> oppIds = list.stream().map(InternalAssignment::getOpportunityId).collect(Collectors.toSet());
        Map<UUID, String> oppTitles = opportunityRepository.findAllById(oppIds).stream()
                .collect(Collectors.toMap(InternalOpportunity::getId, InternalOpportunity::getTitle));

        return list.stream()
                .map(a -> toAssignmentResponse(companyId, a, oppTitles.getOrDefault(a.getOpportunityId(), "Unknown Opportunity")))
                .collect(Collectors.toList());
    }

    @Transactional
    public InternalAssignmentResponse completeAssignment(UUID companyId, UUID assignmentId, CompleteAssignmentRequest request) {
        InternalAssignment assignment = assignmentRepository.findByIdAndCompanyId(assignmentId, companyId)
                .orElseThrow(() -> new AppException(RecruitmentErrorCode.INTERNAL_ASSIGNMENT_NOT_FOUND));

        if (assignment.getStatus() == AssignmentStatus.COMPLETED || assignment.getStatus() == AssignmentStatus.CANCELLED) {
            throw new AppException(RecruitmentErrorCode.INTERNAL_ASSIGNMENT_COMPLETED);
        }

        assignment.setStatus(AssignmentStatus.COMPLETED);
        if (request != null) {
            if (request.getPerformanceRating() != null) assignment.setPerformanceRating(request.getPerformanceRating());
            if (request.getEvaluationNote() != null) assignment.setEvaluationNote(request.getEvaluationNote());
        }

        assignment = assignmentRepository.save(assignment);
        String title = opportunityRepository.findById(assignment.getOpportunityId())
                .map(InternalOpportunity::getTitle).orElse("Unknown Opportunity");
        return toAssignmentResponse(companyId, assignment, title);
    }

    @Transactional(readOnly = true)
    public MarketplaceStatsResponse getStats(UUID companyId) {
        long totalOpps = opportunityRepository.countByCompanyId(companyId);
        long openOpps = opportunityRepository.countByCompanyIdAndStatus(companyId, OpportunityStatus.OPEN);
        long totalApps = applicationRepository.countByCompanyId(companyId);
        long activeAssignments = assignmentRepository.countByCompanyIdAndStatus(companyId, AssignmentStatus.ACTIVE);
        long completedAssignments = assignmentRepository.countByCompanyIdAndStatus(companyId, AssignmentStatus.COMPLETED);

        return MarketplaceStatsResponse.builder()
                .totalOpportunities(totalOpps)
                .openOpportunities(openOpps)
                .totalApplications(totalApps)
                .activeAssignments(activeAssignments)
                .completedAssignments(completedAssignments)
                .build();
    }

    private List<InternalOpportunityResponse> enrichOpportunities(UUID companyId, List<InternalOpportunity> list) {
        Set<UUID> deptIds = list.stream().map(InternalOpportunity::getDepartmentId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<UUID> mgrIds = list.stream().map(InternalOpportunity::getManagerId).filter(Objects::nonNull).collect(Collectors.toSet());

        Map<UUID, String> deptMap = new HashMap<>();
        try {
            if (!deptIds.isEmpty()) {
                orgUnitService.getUnitSummaries(deptIds)
                        .forEach((id, summary) -> deptMap.put(id, summary.getName()));
            }
        } catch (Exception e) {
            log.debug("Could not fetch dept names: {}", e.getMessage());
        }

        Map<UUID, EmployeeSummary> mgrMap = Collections.emptyMap();
        try {
            if (!mgrIds.isEmpty()) {
                mgrMap = employeeService.getEmployeeSummaries(mgrIds);
            }
        } catch (Exception e) {
            log.debug("Could not fetch mgr summaries: {}", e.getMessage());
        }

        final Map<UUID, EmployeeSummary> finalMgrMap = mgrMap;

        return list.stream().map(o -> {
            InternalOpportunityResponse res = new InternalOpportunityResponse();
            res.setId(o.getId());
            res.setCompanyId(o.getCompanyId());
            res.setTitle(o.getTitle());
            res.setType(o.getType());
            res.setDepartmentId(o.getDepartmentId());
            res.setDepartmentName(deptMap.get(o.getDepartmentId()));
            res.setManagerId(o.getManagerId());
            if (o.getManagerId() != null && finalMgrMap.containsKey(o.getManagerId())) {
                res.setManagerName(finalMgrMap.get(o.getManagerId()).getFullName());
            }
            res.setDescription(o.getDescription());
            res.setRequiredSkills(o.getRequiredSkills());
            res.setCommitmentPercentage(o.getCommitmentPercentage());
            res.setStartDate(o.getStartDate());
            res.setEndDate(o.getEndDate());
            res.setStatus(o.getStatus());
            res.setCreatedAt(o.getCreatedAt());
            res.setUpdatedAt(o.getUpdatedAt());
            return res;
        }).collect(Collectors.toList());
    }

    private InternalApplicationResponse toApplicationResponse(UUID companyId, InternalApplication app, String opportunityTitle) {
        EmployeeSummary empSummary = null;
        try {
            empSummary = employeeService.getEmployeeSummary(app.getEmployeeId());
        } catch (Exception e) {
            log.debug("Could not fetch employee summary: {}", e.getMessage());
        }

        return InternalApplicationResponse.builder()
                .id(app.getId())
                .companyId(companyId)
                .opportunityId(app.getOpportunityId())
                .opportunityTitle(opportunityTitle)
                .employeeId(app.getEmployeeId())
                .employeeName(empSummary != null ? empSummary.getFullName() : null)
                .employeeCode(empSummary != null ? empSummary.getEmployeeCode() : null)
                .note(app.getNote())
                .status(app.getStatus())
                .feedback(app.getFeedback())
                .createdAt(app.getCreatedAt())
                .build();
    }

    private InternalAssignmentResponse toAssignmentResponse(UUID companyId, InternalAssignment a, String opportunityTitle) {
        EmployeeSummary empSummary = null;
        try {
            empSummary = employeeService.getEmployeeSummary(a.getEmployeeId());
        } catch (Exception e) {
            log.debug("Could not fetch employee summary: {}", e.getMessage());
        }

        return InternalAssignmentResponse.builder()
                .id(a.getId())
                .companyId(companyId)
                .opportunityId(a.getOpportunityId())
                .opportunityTitle(opportunityTitle)
                .employeeId(a.getEmployeeId())
                .employeeName(empSummary != null ? empSummary.getFullName() : null)
                .employeeCode(empSummary != null ? empSummary.getEmployeeCode() : null)
                .startDate(a.getStartDate())
                .endDate(a.getEndDate())
                .status(a.getStatus())
                .performanceRating(a.getPerformanceRating())
                .evaluationNote(a.getEvaluationNote())
                .createdAt(a.getCreatedAt())
                .build();
    }
}
