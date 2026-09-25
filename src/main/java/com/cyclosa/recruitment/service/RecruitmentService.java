package com.cyclosa.recruitment.service;

import com.cyclosa.common.dto.summary.EmployeeSummary;
import com.cyclosa.common.enums.DataScope;
import com.cyclosa.common.exception.AppException;
import com.cyclosa.common.response.PageData;
import com.cyclosa.common.security.SecurityPermissionEvaluator;
import com.cyclosa.common.util.PageableUtils;
import com.cyclosa.common.util.SecurityUtils;
import com.cyclosa.employee.dto.request.CreateEmployeeRequest;
import com.cyclosa.employee.dto.response.EmployeeDetailResponse;
import com.cyclosa.employee.service.EmployeeService;
import com.cyclosa.organization.service.OrganizationalUnitService;
import com.cyclosa.organization.service.PositionService;
import com.cyclosa.recruitment.dto.request.*;
import com.cyclosa.recruitment.dto.response.*;
import com.cyclosa.recruitment.entity.*;
import com.cyclosa.recruitment.enums.*;
import com.cyclosa.recruitment.exception.RecruitmentErrorCode;
import com.cyclosa.recruitment.mapper.*;
import com.cyclosa.recruitment.repository.*;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecruitmentService {

    private final ManpowerRequestRepository manpowerRequestRepository;
    private final JobPositionRepository jobPositionRepository;
    private final JobPostingRepository jobPostingRepository;
    private final CandidateRepository candidateRepository;
    private final ApplicationRepository applicationRepository;
    private final OfferRepository offerRepository;
    private final TalentPoolRepository talentPoolRepository;

    private final ManpowerRequestMapper manpowerRequestMapper;
    private final JobPositionMapper jobPositionMapper;
    private final JobPostingMapper jobPostingMapper;
    private final CandidateMapper candidateMapper;
    private final ApplicationMapper applicationMapper;
    private final OfferMapper offerMapper;

    private final EmployeeService employeeService;
    private final WorkflowEngineService workflowEngineService;
    private final SecurityPermissionEvaluator perm;

    // =========================================================================
    // 1. MANPOWER REQUESTS
    // =========================================================================

    @Transactional(readOnly = true)
    public PageData<ManpowerRequestResponse> getManpowerRequests(UUID companyId, ManpowerRequestFilter filter, Pageable pageable) {
        Specification<ManpowerRequest> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (companyId != null) predicates.add(cb.equal(root.get("companyId"), companyId));
            if (filter != null) {
                if (filter.getStatus() != null) predicates.add(cb.equal(root.get("status"), filter.getStatus()));
                if (filter.getDepartmentId() != null) predicates.add(cb.equal(root.get("departmentId"), filter.getDepartmentId()));
                if (filter.getKeyword() != null && !filter.getKeyword().isBlank()) {
                    String kw = "%" + filter.getKeyword().trim().toLowerCase() + "%";
                    predicates.add(cb.or(
                            cb.like(cb.lower(root.get("requestCode")), kw),
                            cb.like(cb.lower(root.get("title")), kw)
                    ));
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<ManpowerRequest> page = manpowerRequestRepository.findAll(spec, pageable);
        return PageData.of(page, manpowerRequestMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public ManpowerRequestResponse getManpowerRequestById(UUID companyId, UUID id) {
        ManpowerRequest entity = findManpowerRequest(companyId, id);
        return manpowerRequestMapper.toResponse(entity);
    }

    @Transactional
    public ManpowerRequestResponse createManpowerRequest(UUID companyId, CreateManpowerRequest request) {
        UUID effectiveCompanyId = request.getCompanyId() != null ? request.getCompanyId() : companyId;
        if (effectiveCompanyId == null) throw AppException.badRequest("Yêu cầu cung cấp ID công ty");

        String code = generateManpowerRequestCode(effectiveCompanyId);

        ManpowerRequest entity = manpowerRequestMapper.toEntity(request);
        entity.setCompanyId(effectiveCompanyId);
        entity.setRequestCode(code);
        entity.setStatus(ManpowerRequestStatus.PENDING_APPROVAL);

        entity = manpowerRequestRepository.save(entity);

        // Bind with Workflow Engine
        try {
            UUID currentEmpId = SecurityUtils.getCurrentUserIdOptional()
                    .flatMap(employeeService::findEmployeeIdByUserId)
                    .orElse(null);

            StartWorkflowRequest wfReq = StartWorkflowRequest.builder()
                    .requestType(ApprovalRequestType.MANPOWER_REQUEST)
                    .requestId(entity.getId())
                    .companyId(effectiveCompanyId)
                    .requesterEmployeeId(currentEmpId != null ? currentEmpId : entity.getId())
                    .contextVariables(Map.of(
                            "requestCode", entity.getRequestCode(),
                            "quantity", entity.getQuantity().toString(),
                            "departmentId", entity.getDepartmentId().toString(),
                            "positionId", entity.getPositionId().toString()
                    ))
                    .build();
            WorkflowInstanceResponse instance = workflowEngineService.startWorkflow(wfReq);
            if (instance != null) {
                entity.setWorkflowInstanceId(instance.getId());
                entity = manpowerRequestRepository.save(entity);
            }
        } catch (Exception e) {
            log.warn("Không thể kích hoạt tự động luồng workflow cho ManpowerRequest id={}: {}", entity.getId(), e.getMessage());
        }

        log.info("Created ManpowerRequest id={}, code={}, companyId={}", entity.getId(), entity.getRequestCode(), effectiveCompanyId);
        return manpowerRequestMapper.toResponse(entity);
    }

    @Transactional
    public void cancelManpowerRequest(UUID companyId, UUID id) {
        ManpowerRequest entity = findManpowerRequest(companyId, id);
        if (entity.getStatus() != ManpowerRequestStatus.DRAFT && entity.getStatus() != ManpowerRequestStatus.PENDING_APPROVAL) {
            throw new AppException(RecruitmentErrorCode.MANPOWER_REQUEST_INVALID_STATUS);
        }
        entity.setStatus(ManpowerRequestStatus.CANCELLED);
        manpowerRequestRepository.save(entity);
    }

    @Transactional
    public void handleWorkflowCompleted(UUID workflowInstanceId, UUID requestId, ApprovalStatus status, String comment) {
        Optional<ManpowerRequest> opt = requestId != null
                ? manpowerRequestRepository.findById(requestId)
                : manpowerRequestRepository.findByWorkflowInstanceId(workflowInstanceId);

        if (opt.isEmpty()) return;

        ManpowerRequest mpr = opt.get();
        if (status == ApprovalStatus.APPROVED) {
            mpr.setStatus(ManpowerRequestStatus.APPROVED);
        } else if (status == ApprovalStatus.REJECTED) {
            mpr.setStatus(ManpowerRequestStatus.REJECTED);
        }
        manpowerRequestRepository.save(mpr);
        log.info("Updated ManpowerRequest id={} status to {} from workflow completed", mpr.getId(), mpr.getStatus());
    }

    private ManpowerRequest findManpowerRequest(UUID companyId, UUID id) {
        if (companyId != null) {
            return manpowerRequestRepository.findByIdAndCompanyId(id, companyId)
                    .orElseThrow(() -> new AppException(RecruitmentErrorCode.MANPOWER_REQUEST_NOT_FOUND));
        }
        return manpowerRequestRepository.findById(id)
                .orElseThrow(() -> new AppException(RecruitmentErrorCode.MANPOWER_REQUEST_NOT_FOUND));
    }

    private String generateManpowerRequestCode(UUID companyId) {
        int year = Year.now().getValue();
        long count = manpowerRequestRepository.countByCompanyId(companyId);
        return String.format("MPR-%d-%04d", year, count + 1);
    }

    // =========================================================================
    // 2. JOB POSITIONS
    // =========================================================================

    @Transactional(readOnly = true)
    public PageData<JobPositionResponse> getJobPositions(UUID companyId, JobPositionFilter filter, Pageable pageable) {
        Specification<JobPosition> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (companyId != null) predicates.add(cb.equal(root.get("companyId"), companyId));
            if (filter != null) {
                if (filter.getStatus() != null) predicates.add(cb.equal(root.get("status"), filter.getStatus()));
                if (filter.getDepartmentId() != null) predicates.add(cb.equal(root.get("departmentId"), filter.getDepartmentId()));
                if (filter.getKeyword() != null && !filter.getKeyword().isBlank()) {
                    predicates.add(cb.like(cb.lower(root.get("title")), "%" + filter.getKeyword().trim().toLowerCase() + "%"));
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<JobPosition> page = jobPositionRepository.findAll(spec, pageable);
        return PageData.of(page, jobPositionMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public JobPositionResponse getJobPositionById(UUID companyId, UUID id) {
        JobPosition entity = findJobPosition(companyId, id);
        return jobPositionMapper.toResponse(entity);
    }

    @Transactional
    public JobPositionResponse createJobPosition(UUID companyId, CreateJobPositionRequest request) {
        UUID effectiveCompanyId = request.getCompanyId() != null ? request.getCompanyId() : companyId;
        if (effectiveCompanyId == null) throw AppException.badRequest("Yêu cầu cung cấp ID công ty");

        if (request.getManpowerRequestId() != null) {
            ManpowerRequest mpr = findManpowerRequest(effectiveCompanyId, request.getManpowerRequestId());
            if (mpr.getStatus() != ManpowerRequestStatus.APPROVED) {
                throw new AppException(RecruitmentErrorCode.MANPOWER_REQUEST_NOT_APPROVED);
            }
        }

        JobPosition entity = jobPositionMapper.toEntity(request);
        entity.setCompanyId(effectiveCompanyId);
        entity.setStatus(JobPositionStatus.ACTIVE);

        entity = jobPositionRepository.save(entity);
        log.info("Created JobPosition id={}, title={}, companyId={}", entity.getId(), entity.getTitle(), effectiveCompanyId);
        return jobPositionMapper.toResponse(entity);
    }

    @Transactional
    public JobPositionResponse updateJobPositionStatus(UUID companyId, UUID id, JobPositionStatus status) {
        JobPosition entity = findJobPosition(companyId, id);
        entity.setStatus(status);
        entity = jobPositionRepository.save(entity);
        return jobPositionMapper.toResponse(entity);
    }

    private JobPosition findJobPosition(UUID companyId, UUID id) {
        if (companyId != null) {
            return jobPositionRepository.findByIdAndCompanyId(id, companyId)
                    .orElseThrow(() -> new AppException(RecruitmentErrorCode.JOB_POSITION_NOT_FOUND));
        }
        return jobPositionRepository.findById(id)
                .orElseThrow(() -> new AppException(RecruitmentErrorCode.JOB_POSITION_NOT_FOUND));
    }

    // =========================================================================
    // 3. JOB POSTINGS
    // =========================================================================

    @Transactional(readOnly = true)
    public PageData<JobPostingResponse> getJobPostings(UUID companyId, JobPostingFilter filter, Pageable pageable) {
        Specification<JobPosting> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (companyId != null) predicates.add(cb.equal(root.get("companyId"), companyId));
            if (filter != null) {
                if (filter.getStatus() != null) predicates.add(cb.equal(root.get("status"), filter.getStatus()));
                if (filter.getChannel() != null) predicates.add(cb.equal(root.get("channel"), filter.getChannel()));
                if (filter.getJobPositionId() != null) predicates.add(cb.equal(root.get("jobPositionId"), filter.getJobPositionId()));
                if (filter.getKeyword() != null && !filter.getKeyword().isBlank()) {
                    predicates.add(cb.like(cb.lower(root.get("title")), "%" + filter.getKeyword().trim().toLowerCase() + "%"));
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<JobPosting> page = jobPostingRepository.findAll(spec, pageable);
        return PageData.of(page, jobPostingMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public JobPostingResponse getJobPostingById(UUID companyId, UUID id) {
        JobPosting entity = findJobPosting(companyId, id);
        return jobPostingMapper.toResponse(entity);
    }

    @Transactional
    public JobPostingResponse createJobPosting(UUID companyId, CreateJobPostingRequest request) {
        UUID effectiveCompanyId = request.getCompanyId() != null ? request.getCompanyId() : companyId;
        if (effectiveCompanyId == null) throw AppException.badRequest("Yêu cầu cung cấp ID công ty");

        JobPosition position = findJobPosition(effectiveCompanyId, request.getJobPositionId());
        if (position.getStatus() != JobPositionStatus.ACTIVE) {
            throw new AppException(RecruitmentErrorCode.JOB_POSITION_INACTIVE);
        }

        JobPosting entity = jobPostingMapper.toEntity(request);
        entity.setCompanyId(effectiveCompanyId);
        entity.setStatus(PostingStatus.DRAFT);
        entity.setApplyCount(0);

        entity = jobPostingRepository.save(entity);
        log.info("Created JobPosting id={}, title={}", entity.getId(), entity.getTitle());
        return jobPostingMapper.toResponse(entity);
    }

    @Transactional
    public JobPostingResponse updateJobPostingStatus(UUID companyId, UUID id, PostingStatus status) {
        JobPosting entity = findJobPosting(companyId, id);
        entity.setStatus(status);
        if (status == PostingStatus.PUBLISHED && entity.getPublishedAt() == null) {
            entity.setPublishedAt(LocalDateTime.now());
        }
        entity = jobPostingRepository.save(entity);
        return jobPostingMapper.toResponse(entity);
    }

    @Transactional
    public void incrementApplyCount(UUID jobPostingId) {
        jobPostingRepository.findById(jobPostingId).ifPresent(p -> {
            p.setApplyCount((p.getApplyCount() != null ? p.getApplyCount() : 0) + 1);
            jobPostingRepository.save(p);
        });
    }

    private JobPosting findJobPosting(UUID companyId, UUID id) {
        if (companyId != null) {
            return jobPostingRepository.findByIdAndCompanyId(id, companyId)
                    .orElseThrow(() -> new AppException(RecruitmentErrorCode.JOB_POSTING_NOT_FOUND));
        }
        return jobPostingRepository.findById(id)
                .orElseThrow(() -> new AppException(RecruitmentErrorCode.JOB_POSTING_NOT_FOUND));
    }

    // =========================================================================
    // 4. CANDIDATES
    // =========================================================================

    @Transactional(readOnly = true)
    public PageData<CandidateResponse> getCandidates(UUID companyId, CandidateFilter filter, Pageable pageable) {
        Specification<Candidate> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (companyId != null) predicates.add(cb.equal(root.get("companyId"), companyId));
            if (filter != null) {
                if (filter.getStatus() != null) predicates.add(cb.equal(root.get("status"), filter.getStatus()));
                if (filter.getSource() != null) predicates.add(cb.equal(root.get("source"), filter.getSource()));
                if (filter.getKeyword() != null && !filter.getKeyword().isBlank()) {
                    String kw = "%" + filter.getKeyword().trim().toLowerCase() + "%";
                    predicates.add(cb.or(
                            cb.like(cb.lower(root.get("fullName")), kw),
                            cb.like(cb.lower(root.get("email")), kw),
                            cb.like(cb.lower(root.get("phone")), kw)
                    ));
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Candidate> page = candidateRepository.findAll(spec, pageable);
        return PageData.of(page, candidateMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public CandidateResponse getCandidateById(UUID companyId, UUID id) {
        Candidate entity = findCandidate(companyId, id);
        return candidateMapper.toResponse(entity);
    }

    @Transactional
    public CandidateResponse createCandidate(UUID companyId, CreateCandidateRequest request) {
        UUID effectiveCompanyId = request.getCompanyId() != null ? request.getCompanyId() : companyId;
        if (effectiveCompanyId == null) throw AppException.badRequest("Yêu cầu cung cấp ID công ty");

        if (candidateRepository.existsByCompanyIdAndEmail(effectiveCompanyId, request.getEmail())) {
            throw new AppException(RecruitmentErrorCode.CANDIDATE_EMAIL_EXISTS);
        }

        Candidate entity = candidateMapper.toEntity(request);
        entity.setCompanyId(effectiveCompanyId);
        entity.setStatus(CandidateStatus.ACTIVE);

        entity = candidateRepository.save(entity);
        log.info("Created Candidate id={}, email={}", entity.getId(), entity.getEmail());
        return candidateMapper.toResponse(entity);
    }

    @Transactional
    public CandidateResponse updateCandidate(UUID companyId, UUID id, UpdateCandidateRequest request) {
        Candidate entity = findCandidate(companyId, id);

        if (request.getFullName() != null) entity.setFullName(request.getFullName());
        if (request.getPhone() != null) entity.setPhone(request.getPhone());
        if (request.getSource() != null) entity.setSource(request.getSource());
        if (request.getStatus() != null) entity.setStatus(request.getStatus());
        if (request.getNotes() != null) entity.setNotes(request.getNotes());

        entity = candidateRepository.save(entity);
        return candidateMapper.toResponse(entity);
    }

    @Transactional
    public Candidate findOrCreateCandidate(UUID companyId, String fullName, String email, String phone) {
        return candidateRepository.findByCompanyIdAndEmail(companyId, email)
                .orElseGet(() -> {
                    Candidate newCand = Candidate.builder()
                            .companyId(companyId)
                            .fullName(fullName)
                            .email(email)
                            .phone(phone)
                            .source(CandidateSource.CAREER_PAGE)
                            .status(CandidateStatus.ACTIVE)
                            .build();
                    return candidateRepository.save(newCand);
                });
    }

    private Candidate findCandidate(UUID companyId, UUID id) {
        if (companyId != null) {
            return candidateRepository.findByIdAndCompanyId(id, companyId)
                    .orElseThrow(() -> new AppException(RecruitmentErrorCode.CANDIDATE_NOT_FOUND));
        }
        return candidateRepository.findById(id)
                .orElseThrow(() -> new AppException(RecruitmentErrorCode.CANDIDATE_NOT_FOUND));
    }

    // =========================================================================
    // 5. APPLICATIONS (ATS PIPELINE)
    // =========================================================================

    @Transactional(readOnly = true)
    public PageData<ApplicationResponse> getApplications(UUID companyId, ApplicationFilter filter, Pageable pageable) {
        Specification<Application> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (companyId != null) predicates.add(cb.equal(root.get("companyId"), companyId));
            if (filter != null) {
                if (filter.getJobPositionId() != null) predicates.add(cb.equal(root.get("jobPositionId"), filter.getJobPositionId()));
                if (filter.getJobPostingId() != null) predicates.add(cb.equal(root.get("jobPostingId"), filter.getJobPostingId()));
                if (filter.getCandidateId() != null) predicates.add(cb.equal(root.get("candidateId"), filter.getCandidateId()));
                if (filter.getStage() != null) predicates.add(cb.equal(root.get("stage"), filter.getStage()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Application> page = applicationRepository.findAll(spec, pageable);
        if (page.isEmpty()) return PageData.of(page, List.of());

        Set<UUID> candIds = page.getContent().stream().map(Application::getCandidateId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<UUID, Candidate> candMap = candidateRepository.findAllById(candIds).stream().collect(Collectors.toMap(Candidate::getId, c -> c, (a, b) -> a));

        Set<UUID> posIds = page.getContent().stream().map(Application::getJobPositionId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<UUID, JobPosition> posMap = jobPositionRepository.findAllById(posIds).stream().collect(Collectors.toMap(JobPosition::getId, p -> p, (a, b) -> a));

        List<ApplicationResponse> content = page.getContent().stream()
                .map(entity -> {
                    ApplicationResponse res = applicationMapper.toResponse(entity);
                    Candidate cand = candMap.get(entity.getCandidateId());
                    if (cand != null) {
                        res.setCandidateName(cand.getFullName());
                        res.setCandidateEmail(cand.getEmail());
                        res.setCandidatePhone(cand.getPhone());
                    }
                    JobPosition pos = posMap.get(entity.getJobPositionId());
                    if (pos != null) res.setJobPositionTitle(pos.getTitle());
                    return res;
                }).toList();

        return PageData.of(page, content);
    }

    @Transactional(readOnly = true)
    public ApplicationDetailResponse getApplicationById(UUID companyId, UUID id) {
        Application entity = findApplication(companyId, id);
        return toApplicationDetailResponse(entity);
    }

    @Transactional
    public ApplicationDetailResponse submitApplication(UUID companyId, SubmitApplicationRequest request) {
        UUID effectiveCompanyId = request.getCompanyId() != null ? request.getCompanyId() : companyId;
        if (effectiveCompanyId == null) throw AppException.badRequest("Yêu cầu cung cấp ID công ty");

        JobPosition jobPosition = findJobPosition(effectiveCompanyId, request.getJobPositionId());
        if (jobPosition.getStatus() == JobPositionStatus.CLOSED || jobPosition.getStatus() == JobPositionStatus.INACTIVE) {
            throw new AppException(RecruitmentErrorCode.JOB_POSITION_INACTIVE);
        }

        Candidate candidate;
        if (request.getCandidateId() != null) {
            candidate = findCandidate(effectiveCompanyId, request.getCandidateId());
        } else {
            candidate = findOrCreateCandidate(effectiveCompanyId, request.getFullName(), request.getEmail(), request.getPhone());
        }

        boolean alreadyPending = applicationRepository.existsByCompanyIdAndCandidateIdAndJobPositionIdAndStageNotIn(
                effectiveCompanyId, candidate.getId(), jobPosition.getId(),
                List.of(ApplicationStage.REJECTED, ApplicationStage.HIRED)
        );
        if (alreadyPending) throw new AppException(RecruitmentErrorCode.APPLICATION_ALREADY_EXISTS);

        Application application = Application.builder()
                .companyId(effectiveCompanyId)
                .candidateId(candidate.getId())
                .jobPositionId(jobPosition.getId())
                .jobPostingId(request.getJobPostingId())
                .stage(ApplicationStage.APPLIED)
                .resumeUrl(request.getResumeUrl())
                .coverLetter(request.getCoverLetter())
                .appliedAt(LocalDateTime.now())
                .build();

        Application saved = applicationRepository.save(application);
        if (request.getJobPostingId() != null) incrementApplyCount(request.getJobPostingId());

        return toApplicationDetailResponse(saved);
    }

    @Transactional
    public ApplicationDetailResponse updateApplicationStage(UUID companyId, UUID id, UpdateApplicationStageRequest request) {
        Application entity = findApplication(companyId, id);
        entity.setStage(request.getStage());
        if (request.getStage() == ApplicationStage.REJECTED) entity.setRejectionReason(request.getRejectionReason());
        if (request.getScoreMatch() != null) entity.setScoreMatch(request.getScoreMatch());
        Application saved = applicationRepository.save(entity);
        return toApplicationDetailResponse(saved);
    }

    private Application findApplication(UUID companyId, UUID id) {
        if (companyId != null) {
            return applicationRepository.findByIdAndCompanyId(id, companyId)
                    .orElseThrow(() -> new AppException(RecruitmentErrorCode.APPLICATION_NOT_FOUND));
        }
        return applicationRepository.findById(id)
                .orElseThrow(() -> new AppException(RecruitmentErrorCode.APPLICATION_NOT_FOUND));
    }

    private ApplicationDetailResponse toApplicationDetailResponse(Application entity) {
        ApplicationDetailResponse res = applicationMapper.toDetailResponse(entity);
        candidateRepository.findById(entity.getCandidateId()).ifPresent(cand -> {
            res.setCandidateName(cand.getFullName());
            res.setCandidateEmail(cand.getEmail());
            res.setCandidatePhone(cand.getPhone());
        });
        jobPositionRepository.findById(entity.getJobPositionId()).ifPresent(pos -> res.setJobPositionTitle(pos.getTitle()));
        return res;
    }

    // =========================================================================
    // 6. OFFERS & HIRING
    // =========================================================================

    @Transactional(readOnly = true)
    public PageData<OfferResponse> getOffers(UUID companyId, OfferFilter filter, Pageable pageable) {
        Specification<Offer> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (companyId != null) predicates.add(cb.equal(root.get("companyId"), companyId));
            if (filter != null) {
                if (filter.getStatus() != null) predicates.add(cb.equal(root.get("status"), filter.getStatus()));
                if (filter.getApplicationId() != null) predicates.add(cb.equal(root.get("applicationId"), filter.getApplicationId()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Offer> page = offerRepository.findAll(spec, pageable);
        return PageData.of(page, offerMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public OfferResponse getOfferById(UUID companyId, UUID id) {
        Offer entity = findOffer(companyId, id);
        return offerMapper.toResponse(entity);
    }

    @Transactional
    public OfferResponse createOffer(UUID companyId, CreateOfferRequest request) {
        UUID effectiveCompanyId = request.getCompanyId() != null ? request.getCompanyId() : companyId;
        if (effectiveCompanyId == null) throw AppException.badRequest("Yêu cầu cung cấp ID công ty");

        Application app = findApplication(effectiveCompanyId, request.getApplicationId());

        Offer entity = offerMapper.toEntity(request);
        entity.setCompanyId(effectiveCompanyId);
        entity.setStatus(OfferStatus.DRAFT);

        entity = offerRepository.save(entity);

        // Update application stage to OFFER_SENT
        app.setStage(ApplicationStage.OFFER_SENT);
        applicationRepository.save(app);

        return offerMapper.toResponse(entity);
    }

    @Transactional
    public OfferResponse updateOfferStatus(UUID companyId, UUID id, RespondOfferRequest request) {
        Offer entity = findOffer(companyId, id);

        if (request.getStatus() == OfferStatus.ACCEPTED) {
            entity.setStatus(OfferStatus.ACCEPTED);
            Application app = findApplication(companyId, entity.getApplicationId());
            app.setStage(ApplicationStage.OFFER_ACCEPTED);
            applicationRepository.save(app);
        } else if (request.getStatus() == OfferStatus.DECLINED) {
            entity.setStatus(OfferStatus.DECLINED);
            entity.setDeclineReason(request.getDeclineReason());
            Application app = findApplication(companyId, entity.getApplicationId());
            app.setStage(ApplicationStage.REJECTED);
            app.setRejectionReason("Ứng viên từ chối nhận Offer: " + request.getDeclineReason());
            applicationRepository.save(app);
        } else {
            entity.setStatus(request.getStatus());
        }

        entity = offerRepository.save(entity);
        return offerMapper.toResponse(entity);
    }

    private Offer findOffer(UUID companyId, UUID id) {
        if (companyId != null) {
            return offerRepository.findByIdAndCompanyId(id, companyId)
                    .orElseThrow(() -> new AppException(RecruitmentErrorCode.OFFER_NOT_FOUND));
        }
        return offerRepository.findById(id)
                .orElseThrow(() -> new AppException(RecruitmentErrorCode.OFFER_NOT_FOUND));
    }

    @Transactional
    public ConvertToEmployeeResponse convertToEmployee(UUID companyId, UUID applicationId, ConvertToEmployeeRequest request) {
        Application application = findApplication(companyId, applicationId);

        if (application.getStage() != ApplicationStage.OFFER_ACCEPTED && application.getStage() != ApplicationStage.HIRED) {
            throw new AppException(RecruitmentErrorCode.APPLICATION_NOT_OFFER_ACCEPTED);
        }

        Candidate candidate = candidateRepository.findById(application.getCandidateId())
                .orElseThrow(() -> new AppException(RecruitmentErrorCode.CANDIDATE_NOT_FOUND));

        if (candidate.getStatus() == CandidateStatus.HIRED && application.getStage() == ApplicationStage.HIRED) {
            throw new AppException(RecruitmentErrorCode.APPLICATION_ALREADY_HIRED);
        }

        JobPosition jobPosition = jobPositionRepository.findById(application.getJobPositionId())
                .orElseThrow(() -> new AppException(RecruitmentErrorCode.JOB_POSITION_NOT_FOUND));

        // Build CreateEmployeeRequest cleanly
        CreateEmployeeRequest empRequest = new CreateEmployeeRequest();
        empRequest.setCompanyId(application.getCompanyId());
        empRequest.setFullName(candidate.getFullName());
        empRequest.setPersonalEmail(candidate.getEmail());
        empRequest.setPhone(candidate.getPhone());
        empRequest.setNationalIdNumber(request.getNationalIdNumber());
        empRequest.setCompanyEmail(request.getCompanyEmail());
        empRequest.setOrganizationalUnitId(jobPosition.getDepartmentId());
        empRequest.setPositionId(jobPosition.getPositionId());
        empRequest.setBranchId(request.getBranchId());
        empRequest.setJobLevelId(request.getJobLevelId());
        empRequest.setManagerEmployeeId(request.getManagerEmployeeId());
        empRequest.setHireDate(request.getHireDate() != null ? request.getHireDate() : LocalDate.now());
        empRequest.setAutoCreateUser(request.isAutoCreateUser());

        EmployeeDetailResponse empDetail = employeeService.createEmployee(application.getCompanyId(), empRequest);

        application.setStage(ApplicationStage.HIRED);
        applicationRepository.save(application);

        candidate.setStatus(CandidateStatus.HIRED);
        candidateRepository.save(candidate);

        log.info("Successfully converted application id={} (candidate: {}) to employee id={} (code: {})",
                application.getId(), candidate.getFullName(), empDetail.getId(), empDetail.getEmployeeCode());

        return ConvertToEmployeeResponse.builder()
                .applicationId(application.getId())
                .candidateId(candidate.getId())
                .employeeId(empDetail.getId())
                .employeeCode(empDetail.getEmployeeCode())
                .fullName(empDetail.getFullName())
                .companyEmail(empDetail.getCompanyEmail())
                .hireDate(empDetail.getHireDate())
                .message("Tiếp nhận ứng viên thành nhân viên chính thức thành công")
                .build();
    }

    // =========================================================================
    // 7. TALENT POOL
    // =========================================================================

    @Transactional(readOnly = true)
    public PageData<TalentPoolResponse> getTalentPool(UUID companyId, TalentPoolFilter filter, Pageable pageable) {
        Specification<TalentPool> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (companyId != null) predicates.add(cb.equal(root.get("companyId"), companyId));
            if (filter != null) {
                if (filter.getTag() != null && !filter.getTag().isBlank()) {
                    predicates.add(cb.like(cb.lower(root.get("tag")), "%" + filter.getTag().trim().toLowerCase() + "%"));
                }
                if (filter.getKeyword() != null && !filter.getKeyword().isBlank()) {
                    predicates.add(cb.like(cb.lower(root.get("notes")), "%" + filter.getKeyword().trim().toLowerCase() + "%"));
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<TalentPool> page = talentPoolRepository.findAll(spec, pageable);
        if (page.isEmpty()) return PageData.of(page, List.of());

        Set<UUID> candIds = page.getContent().stream().map(TalentPool::getCandidateId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<UUID, Candidate> candMap = candidateRepository.findAllById(candIds).stream().collect(Collectors.toMap(Candidate::getId, c -> c, (a, b) -> a));

        List<TalentPoolResponse> content = page.getContent().stream()
                .map(tp -> {
                    Candidate c = candMap.get(tp.getCandidateId());
                    return TalentPoolResponse.builder()
                            .id(tp.getId())
                            .companyId(tp.getCompanyId())
                            .candidateId(tp.getCandidateId())
                            .candidateName(c != null ? c.getFullName() : null)
                            .candidateEmail(c != null ? c.getEmail() : null)
                            .candidatePhone(c != null ? c.getPhone() : null)
                            .tag(tp.getTag())
                            .notes(tp.getNotes())
                            .createdAt(tp.getCreatedAt())
                            .build();
                }).toList();

        return PageData.of(page, content);
    }

    @Transactional
    public TalentPoolResponse addToTalentPool(UUID companyId, AddToTalentPoolRequest request) {
        UUID effectiveCompanyId = request.getCompanyId() != null ? request.getCompanyId() : companyId;
        if (effectiveCompanyId == null) throw AppException.badRequest("Yêu cầu cung cấp ID công ty");

        Candidate candidate = findCandidate(effectiveCompanyId, request.getCandidateId());

        if (talentPoolRepository.existsByCompanyIdAndCandidateId(effectiveCompanyId, candidate.getId())) {
            throw new AppException(RecruitmentErrorCode.TALENT_POOL_CANDIDATE_EXISTS);
        }

        TalentPool entity = TalentPool.builder()
                .companyId(effectiveCompanyId)
                .candidateId(candidate.getId())
                .tag(request.getTag())
                .notes(request.getNotes())
                .build();

        entity = talentPoolRepository.save(entity);
        return TalentPoolResponse.builder()
                .id(entity.getId())
                .companyId(entity.getCompanyId())
                .candidateId(candidate.getId())
                .candidateName(candidate.getFullName())
                .candidateEmail(candidate.getEmail())
                .candidatePhone(candidate.getPhone())
                .tag(entity.getTag())
                .notes(entity.getNotes())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    @Transactional
    public void removeFromTalentPool(UUID companyId, UUID id) {
        TalentPool entity = talentPoolRepository.findById(id)
                .orElseThrow(() -> new AppException(RecruitmentErrorCode.TALENT_POOL_RECORD_NOT_FOUND));
        talentPoolRepository.delete(entity);
    }
}
