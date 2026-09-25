package com.cyclosa.recruitment.service;

import com.cyclosa.common.dto.summary.EmployeeSummary;
import com.cyclosa.common.exception.AppException;
import com.cyclosa.common.response.PageData;
import com.cyclosa.employee.service.EmployeeService;
import com.cyclosa.recruitment.dto.request.*;
import com.cyclosa.recruitment.dto.response.*;
import com.cyclosa.recruitment.entity.*;
import com.cyclosa.recruitment.enums.*;
import com.cyclosa.recruitment.exception.RecruitmentErrorCode;
import com.cyclosa.recruitment.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewService {

    private final InterviewKitRepository kitRepository;
    private final InterviewQuestionRepository questionRepository;
    private final JobPositionRepository jobPositionRepository;
    private final InterviewRepository interviewRepository;
    private final InterviewPanelMemberRepository panelMemberRepository;
    private final InterviewEvaluationRepository evaluationRepository;
    private final EmployeeService employeeService;

    // =========================================================================
    // 1. INTERVIEW KITS & QUESTIONS
    // =========================================================================

    @Transactional(readOnly = true)
    public PageData<InterviewKitResponse> getInterviewKits(UUID companyId, UUID jobPositionId, Pageable pageable) {
        Page<InterviewKit> page;
        if (jobPositionId != null) {
            List<InterviewKit> list = kitRepository.findByCompanyIdAndJobPositionId(companyId, jobPositionId);
            page = new PageImpl<>(list, pageable, list.size());
        } else {
            page = kitRepository.findByCompanyId(companyId, pageable);
        }
        return PageData.of(page, this::toKitResponse);
    }

    @Transactional(readOnly = true)
    public InterviewKitResponse getInterviewKitById(UUID companyId, UUID id) {
        InterviewKit entity = findKit(companyId, id);
        return toKitResponse(entity);
    }

    @Transactional
    public InterviewKitResponse createInterviewKit(UUID companyId, CreateInterviewKitRequest request) {
        UUID effectiveCompanyId = request.getCompanyId() != null ? request.getCompanyId() : companyId;
        if (effectiveCompanyId == null) throw AppException.badRequest("Yêu cầu cung cấp ID công ty");

        if (request.getJobPositionId() != null) {
            jobPositionRepository.findByIdAndCompanyId(request.getJobPositionId(), effectiveCompanyId)
                    .orElseThrow(() -> new AppException(RecruitmentErrorCode.JOB_POSITION_NOT_FOUND));
        }

        InterviewKit entity = InterviewKit.builder()
                .companyId(effectiveCompanyId)
                .jobPositionId(request.getJobPositionId())
                .title(request.getTitle())
                .interviewType(request.getInterviewType())
                .description(request.getDescription())
                .build();

        entity = kitRepository.save(entity);

        List<InterviewQuestionResponse> questionResponses = new ArrayList<>();
        if (request.getQuestions() != null && !request.getQuestions().isEmpty()) {
            int order = 1;
            for (CreateInterviewQuestionRequest qReq : request.getQuestions()) {
                InterviewQuestion q = InterviewQuestion.builder()
                        .interviewKit(entity)
                        .questionText(qReq.getQuestionText())
                        .criteria(qReq.getCriteria())
                        .maxScore(qReq.getMaxScore() != null ? qReq.getMaxScore() : 5)
                        .weight(qReq.getWeight() != null ? qReq.getWeight() : java.math.BigDecimal.ONE)
                        .sortOrder(qReq.getSortOrder() != null ? qReq.getSortOrder() : order++)
                        .build();
                q = questionRepository.save(q);
                questionResponses.add(toQuestionResponse(q));
            }
        }

        log.info("Created InterviewKit id={}, title={}, companyId={}", entity.getId(), entity.getTitle(), effectiveCompanyId);
        InterviewKitResponse res = toKitResponse(entity);
        res.setQuestions(questionResponses);
        return res;
    }

    @Transactional
    public InterviewKitResponse updateInterviewKit(UUID companyId, UUID id, UpdateInterviewKitRequest request) {
        InterviewKit entity = findKit(companyId, id);
        if (request.getTitle() != null) entity.setTitle(request.getTitle());
        if (request.getInterviewType() != null) entity.setInterviewType(request.getInterviewType());
        if (request.getDescription() != null) entity.setDescription(request.getDescription());
        entity = kitRepository.save(entity);
        return toKitResponse(entity);
    }

    @Transactional
    public void deleteInterviewKit(UUID companyId, UUID id) {
        InterviewKit entity = findKit(companyId, id);
        kitRepository.delete(entity);
    }

    @Transactional
    public InterviewQuestionResponse addQuestionToKit(UUID companyId, UUID kitId, CreateInterviewQuestionRequest request) {
        InterviewKit kit = findKit(companyId, kitId);
        InterviewQuestion q = InterviewQuestion.builder()
                .interviewKit(kit)
                .questionText(request.getQuestionText())
                .criteria(request.getCriteria())
                .maxScore(request.getMaxScore() != null ? request.getMaxScore() : 5)
                .weight(request.getWeight() != null ? request.getWeight() : java.math.BigDecimal.ONE)
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .build();
        q = questionRepository.save(q);
        return toQuestionResponse(q);
    }

    @Transactional
    public void deleteQuestion(UUID companyId, UUID questionId) {
        InterviewQuestion q = questionRepository.findById(questionId)
                .orElseThrow(() -> new AppException(RecruitmentErrorCode.INTERVIEW_QUESTION_NOT_FOUND));
        questionRepository.delete(q);
    }

    private InterviewKit findKit(UUID companyId, UUID id) {
        if (companyId != null) {
            return kitRepository.findByIdAndCompanyId(id, companyId)
                    .orElseThrow(() -> new AppException(RecruitmentErrorCode.INTERVIEW_KIT_NOT_FOUND));
        }
        return kitRepository.findById(id)
                .orElseThrow(() -> new AppException(RecruitmentErrorCode.INTERVIEW_KIT_NOT_FOUND));
    }

    private InterviewKitResponse toKitResponse(InterviewKit entity) {
        List<InterviewQuestionResponse> questions = questionRepository.findByInterviewKitIdOrderBySortOrderAsc(entity.getId())
                .stream().map(this::toQuestionResponse).toList();
        return InterviewKitResponse.builder()
                .id(entity.getId())
                .companyId(entity.getCompanyId())
                .jobPositionId(entity.getJobPositionId())
                .title(entity.getTitle())
                .interviewType(entity.getInterviewType())
                .description(entity.getDescription())
                .questions(questions)
                .createdAt(entity.getCreatedAt())
                .build();
    }

    private InterviewQuestionResponse toQuestionResponse(InterviewQuestion q) {
        return InterviewQuestionResponse.builder()
                .id(q.getId())
                .questionText(q.getQuestionText())
                .criteria(q.getCriteria())
                .maxScore(q.getMaxScore())
                .weight(q.getWeight())
                .sortOrder(q.getSortOrder())
                .build();
    }

    // =========================================================================
    // 2. INTERVIEWS & EVALUATIONS (PANEL & BLIND GRADING)
    // =========================================================================

    @Transactional(readOnly = true)
    public List<InterviewResponse> getInterviewsByApplication(UUID companyId, UUID applicationId) {
        List<Interview> list = interviewRepository.findByApplicationIdOrderByRoundNumberAsc(applicationId);
        return list.stream().map(this::toInterviewResponse).toList();
    }

    @Transactional(readOnly = true)
    public InterviewResponse getInterviewById(UUID companyId, UUID id) {
        Interview entity = findInterview(companyId, id);
        return toInterviewResponse(entity);
    }

    @Transactional
    public InterviewResponse scheduleInterview(UUID companyId, ScheduleInterviewRequest request) {
        UUID effectiveCompanyId = request.getCompanyId() != null ? request.getCompanyId() : companyId;
        if (effectiveCompanyId == null) throw AppException.badRequest("Yêu cầu cung cấp ID công ty");

        Interview interview = Interview.builder()
                .companyId(effectiveCompanyId)
                .applicationId(request.getApplicationId())
                .interviewKitId(request.getInterviewKitId())
                .interviewType(request.getInterviewType())
                .roundNumber(request.getRoundNumber() != null ? request.getRoundNumber() : 1)
                .scheduledStartTime(request.getScheduledStartTime())
                .scheduledEndTime(request.getScheduledEndTime())
                .locationOrMeetingUrl(request.getLocationOrMeetingUrl())
                .status(InterviewStatus.SCHEDULED)
                .notes(request.getNotes())
                .build();

        interview = interviewRepository.save(interview);

        List<InterviewPanelMember> panelMembers = new ArrayList<>();
        if (request.getPanelInterviewerEmployeeIds() != null && !request.getPanelInterviewerEmployeeIds().isEmpty()) {
            for (UUID empId : request.getPanelInterviewerEmployeeIds()) {
                InterviewPanelMember member = InterviewPanelMember.builder()
                        .interview(interview)
                        .interviewerEmployeeId(empId)
                        .role("INTERVIEWER")
                        .build();
                panelMembers.add(panelMemberRepository.save(member));
            }
        }

        log.info("Scheduled interview id={}, type={}, round={}", interview.getId(), interview.getInterviewType(), interview.getRoundNumber());
        return toInterviewResponse(interview);
    }

    @Transactional
    public EvaluationResponse submitEvaluation(UUID companyId, UUID interviewId, UUID interviewerEmployeeId, SubmitEvaluationRequest request) {
        Interview interview = findInterview(companyId, interviewId);

        if (interview.getStatus() == InterviewStatus.CANCELLED) {
            throw new AppException(RecruitmentErrorCode.INTERVIEW_INVALID_STATUS);
        }

        List<InterviewPanelMember> panel = panelMemberRepository.findByInterviewId(interviewId);
        boolean inPanel = panel.stream().anyMatch(m -> m.getInterviewerEmployeeId().equals(interviewerEmployeeId));
        if (!inPanel) {
            throw new AppException(RecruitmentErrorCode.INTERVIEWER_NOT_IN_PANEL);
        }

        if (evaluationRepository.findByInterviewIdAndInterviewerEmployeeId(interviewId, interviewerEmployeeId).isPresent()) {
            throw new AppException(RecruitmentErrorCode.INTERVIEW_EVALUATION_EXISTS);
        }

        InterviewEvaluation eval = InterviewEvaluation.builder()
                .interviewId(interviewId)
                .interviewerEmployeeId(interviewerEmployeeId)
                .overallScore(request.getOverallScore())
                .overallRecommendation(request.getOverallRecommendation())
                .feedbackData(request.getFeedbackData())
                .notes(request.getNotes())
                .build();

        eval = evaluationRepository.save(eval);

        // Update interview status if COMPLETED
        if (interview.getStatus() == InterviewStatus.SCHEDULED) {
            interview.setStatus(InterviewStatus.COMPLETED);
            interviewRepository.save(interview);
        }

        log.info("Interviewer id={} submitted evaluation for interview id={}, recommendation={}",
                interviewerEmployeeId, interviewId, eval.getOverallRecommendation());

        return toEvaluationResponse(eval);
    }

    @Transactional(readOnly = true)
    public ConsolidatedFeedbackResponse getConsolidatedFeedback(UUID companyId, UUID interviewId) {
        Interview interview = findInterview(companyId, interviewId);
        List<InterviewPanelMember> panel = panelMemberRepository.findByInterviewId(interviewId);
        List<InterviewEvaluation> evals = evaluationRepository.findByInterviewId(interviewId);

        Set<UUID> empIds = panel.stream().map(InterviewPanelMember::getInterviewerEmployeeId).collect(Collectors.toSet());
        Map<UUID, EmployeeSummary> empMap = Collections.emptyMap();
        try {
            if (!empIds.isEmpty()) {
                empMap = employeeService.getEmployeeSummaries(empIds);
            }
        } catch (Exception e) {
            log.debug("Could not enrich employee summaries: {}", e.getMessage());
        }

        Map<InterviewRecommendation, Long> recCounts = evals.stream()
                .collect(Collectors.groupingBy(InterviewEvaluation::getOverallRecommendation, Collectors.counting()));

        BigDecimal avgScore = null;
        if (!evals.isEmpty()) {
            double avg = evals.stream()
                    .filter(e -> e.getOverallScore() != null)
                    .mapToDouble(e -> e.getOverallScore().doubleValue())
                    .average()
                    .orElse(0.0);
            avgScore = BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP);
        }

        // Divergence Alert Detection
        boolean divergenceAlert = false;
        String divergenceMsg = null;

        boolean hasStrongYes = evals.stream().anyMatch(e -> e.getOverallRecommendation() == InterviewRecommendation.STRONG_YES);
        boolean hasStrongNo = evals.stream().anyMatch(e -> e.getOverallRecommendation() == InterviewRecommendation.STRONG_NO);

        if (hasStrongYes && hasStrongNo) {
            divergenceAlert = true;
            divergenceMsg = "Cảnh báo chênh lệch bất thường (Divergence Alert): Tồn tại ý kiến trái ngược gay gắt (STRONG_YES đối đầu STRONG_NO). Cần tổ chức phiên Debrief để giải trình.";
        } else if (evals.size() >= 2) {
            DoubleSummaryStatistics stats = evals.stream()
                    .filter(e -> e.getOverallScore() != null)
                    .mapToDouble(e -> e.getOverallScore().doubleValue())
                    .summaryStatistics();
            if (stats.getMax() - stats.getMin() >= 2.0) {
                divergenceAlert = true;
                divergenceMsg = String.format("Cảnh báo chênh lệch bất thường: Độ lệch điểm số giữa các giám khảo lớn hơn 2.0 điểm (Max=%.1f, Min=%.1f).", stats.getMax(), stats.getMin());
            }
        }

        final Map<UUID, EmployeeSummary> finalEmpMap = empMap;
        List<EvaluationResponse> evalResponses = evals.stream().map(e -> {
            EvaluationResponse res = toEvaluationResponse(e);
            if (finalEmpMap.containsKey(e.getInterviewerEmployeeId())) {
                EmployeeSummary s = finalEmpMap.get(e.getInterviewerEmployeeId());
                res.setInterviewerName(s.getFullName());
            }
            return res;
        }).toList();

        return ConsolidatedFeedbackResponse.builder()
                .interviewId(interviewId)
                .totalPanelMembers(panel.size())
                .submittedEvaluations(evals.size())
                .averageScore(avgScore)
                .divergenceAlert(divergenceAlert)
                .divergenceMessage(divergenceMsg)
                .evaluations(evalResponses)
                .build();
    }

    @Transactional(readOnly = true)
    public InterviewerScoringPatternResponse getInterviewerScoringPattern(UUID interviewerId) {
        List<InterviewEvaluation> evals = evaluationRepository.findByInterviewerEmployeeId(interviewerId);
        int total = evals.size();
        BigDecimal avgScore = BigDecimal.ZERO;
        Map<String, Long> counts = Collections.emptyMap();
        String tendency = "CHƯA ĐỦ DỮ LIỆU ĐÁNH GIÁ";

        if (total > 0) {
            double avg = evals.stream()
                    .filter(e -> e.getOverallScore() != null)
                    .mapToDouble(e -> e.getOverallScore().doubleValue())
                    .average()
                    .orElse(0.0);
            avgScore = BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP);
            counts = evals.stream()
                    .collect(Collectors.groupingBy(e -> e.getOverallRecommendation().name(), Collectors.counting()));

            long yesCount = evals.stream().filter(e -> e.getOverallRecommendation() == InterviewRecommendation.YES || e.getOverallRecommendation() == InterviewRecommendation.STRONG_YES).count();
            double yesRatio = (double) yesCount / total;

            if (total >= 3) {
                if (avg < 2.5 || yesRatio < 0.25) {
                    tendency = "KHẮT KHE (Hawkish) - Tỷ lệ cho điểm và đồng ý thấp hơn mặt bằng chung";
                } else if (avg > 4.2 || yesRatio > 0.85) {
                    tendency = "DỄ DÃI (Dovish) - Tỷ lệ cho điểm và đồng ý cao hơn mặt bằng chung";
                } else {
                    tendency = "CÂN BẰNG (Balanced) - Thang điểm và quyết định phân bổ đồng đều";
                }
            }
        }

        String interviewerName = null;
        try {
            EmployeeSummary s = employeeService.getEmployeeSummary(interviewerId);
            if (s != null) interviewerName = s.getFullName();
        } catch (Exception e) {
            log.debug("Could not get employee summary for interviewer: {}", e.getMessage());
        }

        return InterviewerScoringPatternResponse.builder()
                .interviewerEmployeeId(interviewerId)
                .interviewerName(interviewerName)
                .totalEvaluations(total)
                .averageScore(avgScore)
                .recommendationDistribution(counts)
                .build();
    }

    private Interview findInterview(UUID companyId, UUID id) {
        if (companyId != null) {
            return interviewRepository.findByIdAndCompanyId(id, companyId)
                    .orElseThrow(() -> new AppException(RecruitmentErrorCode.INTERVIEW_NOT_FOUND));
        }
        return interviewRepository.findById(id)
                .orElseThrow(() -> new AppException(RecruitmentErrorCode.INTERVIEW_NOT_FOUND));
    }

    private InterviewResponse toInterviewResponse(Interview entity) {
        List<InterviewPanelMember> panelList = panelMemberRepository.findByInterviewId(entity.getId());
        Set<UUID> empIds = panelList.stream().map(InterviewPanelMember::getInterviewerEmployeeId).collect(Collectors.toSet());
        Map<UUID, EmployeeSummary> empMap = employeeService.getEmployeeSummaries(empIds);

        List<InterviewPanelMemberResponse> panelMembers = panelList.stream().map(m -> {
            EmployeeSummary emp = empMap.get(m.getInterviewerEmployeeId());
            return InterviewPanelMemberResponse.builder()
                    .id(m.getId())
                    .interviewerEmployeeId(m.getInterviewerEmployeeId())
                    .interviewerName(emp != null ? emp.getFullName() : null)
                    .interviewerEmail(emp != null ? emp.getCompanyEmail() : null)
                    .role(m.getRole())
                    .build();
        }).toList();

        return InterviewResponse.builder()
                .id(entity.getId())
                .companyId(entity.getCompanyId())
                .applicationId(entity.getApplicationId())
                .interviewKitId(entity.getInterviewKitId())
                .interviewType(entity.getInterviewType())
                .roundNumber(entity.getRoundNumber())
                .scheduledStartTime(entity.getScheduledStartTime())
                .scheduledEndTime(entity.getScheduledEndTime())
                .locationOrMeetingUrl(entity.getLocationOrMeetingUrl())
                .status(entity.getStatus())
                .panelMembers(panelMembers)
                .createdAt(entity.getCreatedAt())
                .build();
    }

    private EvaluationResponse toEvaluationResponse(InterviewEvaluation e) {
        String interviewerName = null;
        try {
            EmployeeSummary emp = employeeService.getEmployeeSummary(e.getInterviewerEmployeeId());
            if (emp != null) interviewerName = emp.getFullName();
        } catch (Exception ex) {
            log.debug("Failed to get interviewer summary: {}", ex.getMessage());
        }

        return EvaluationResponse.builder()
                .id(e.getId())
                .interviewId(e.getInterviewId())
                .interviewerEmployeeId(e.getInterviewerEmployeeId())
                .interviewerName(interviewerName)
                .overallScore(e.getOverallScore())
                .overallRecommendation(e.getOverallRecommendation())
                .feedbackData(e.getFeedbackData())
                .notes(e.getNotes())
                .createdAt(e.getCreatedAt())
                .build();
    }
}
