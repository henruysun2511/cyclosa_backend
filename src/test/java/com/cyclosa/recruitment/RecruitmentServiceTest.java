package com.cyclosa.recruitment;

import com.cyclosa.common.dto.summary.EmployeeSummary;
import com.cyclosa.common.security.SecurityPermissionEvaluator;
import com.cyclosa.employee.dto.request.CreateEmployeeRequest;
import com.cyclosa.employee.dto.response.EmployeeDetailResponse;
import com.cyclosa.employee.service.EmployeeService;
import com.cyclosa.recruitment.dto.request.*;
import com.cyclosa.recruitment.dto.response.*;
import com.cyclosa.recruitment.entity.*;
import com.cyclosa.recruitment.enums.*;
import com.cyclosa.recruitment.mapper.*;
import com.cyclosa.recruitment.repository.*;
import com.cyclosa.recruitment.service.InterviewService;
import com.cyclosa.recruitment.service.RecruitmentService;
import com.cyclosa.workflow.enums.ApprovalStatus;
import com.cyclosa.workflow.service.WorkflowEngineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecruitmentServiceTest {

    @Mock
    private ManpowerRequestRepository requestRepository;
    @Mock
    private JobPositionRepository jobPositionRepository;
    @Mock
    private JobPostingRepository jobPostingRepository;
    @Mock
    private CandidateRepository candidateRepository;
    @Mock
    private ApplicationRepository applicationRepository;
    @Mock
    private OfferRepository offerRepository;
    @Mock
    private TalentPoolRepository talentPoolRepository;

    @Mock
    private ManpowerRequestMapper requestMapper;
    @Mock
    private JobPositionMapper jobPositionMapper;
    @Mock
    private JobPostingMapper jobPostingMapper;
    @Mock
    private CandidateMapper candidateMapper;
    @Mock
    private ApplicationMapper applicationMapper;
    @Mock
    private OfferMapper offerMapper;

    @Mock
    private EmployeeService employeeService;
    @Mock
    private WorkflowEngineService workflowEngineService;
    @Mock
    private SecurityPermissionEvaluator perm;

    private RecruitmentService recruitmentService;

    @Mock
    private InterviewKitRepository kitRepository;
    @Mock
    private InterviewQuestionRepository questionRepository;
    @Mock
    private InterviewRepository interviewRepository;
    @Mock
    private InterviewPanelMemberRepository panelMemberRepository;
    @Mock
    private InterviewEvaluationRepository evaluationRepository;

    private InterviewService interviewService;

    private UUID companyId;
    private UUID departmentId;
    private UUID positionId;

    @BeforeEach
    void setUp() {
        companyId = UUID.randomUUID();
        departmentId = UUID.randomUUID();
        positionId = UUID.randomUUID();

        recruitmentService = new RecruitmentService(
                requestRepository,
                jobPositionRepository,
                jobPostingRepository,
                candidateRepository,
                applicationRepository,
                offerRepository,
                talentPoolRepository,
                requestMapper,
                jobPositionMapper,
                jobPostingMapper,
                candidateMapper,
                applicationMapper,
                offerMapper,
                employeeService,
                workflowEngineService,
                perm
        );

        interviewService = new InterviewService(
                kitRepository,
                questionRepository,
                jobPositionRepository,
                interviewRepository,
                panelMemberRepository,
                evaluationRepository,
                employeeService
        );
    }

    @Test
    @DisplayName("Tạo đề xuất tuyển dụng nhân sự (ManpowerRequest) thành công")
    void testCreateManpowerRequest_Success() {
        CreateManpowerRequest req = CreateManpowerRequest.builder()
                .companyId(companyId)
                .departmentId(departmentId)
                .positionId(positionId)
                .quantity(2)
                .reason("Mở rộng dự án mới")
                .build();

        when(requestRepository.countByCompanyId(companyId)).thenReturn(0L);

        ManpowerRequest entity = ManpowerRequest.builder()
                .departmentId(departmentId)
                .positionId(positionId)
                .quantity(2)
                .reason("Mở rộng dự án mới")
                .build();

        when(requestMapper.toEntity(req)).thenReturn(entity);
        when(requestRepository.save(any(ManpowerRequest.class))).thenAnswer(i -> {
            ManpowerRequest r = i.getArgument(0);
            r.setId(UUID.randomUUID());
            return r;
        });

        ManpowerRequestResponse mockResponse = ManpowerRequestResponse.builder()
                .requestCode("MPR-2026-0001")
                .status(ManpowerRequestStatus.PENDING_APPROVAL)
                .build();
        when(requestMapper.toResponse(any(ManpowerRequest.class))).thenReturn(mockResponse);

        ManpowerRequestResponse response = recruitmentService.createManpowerRequest(companyId, req);

        assertThat(response).isNotNull();
        assertThat(response.getRequestCode()).isEqualTo("MPR-2026-0001");
        assertThat(response.getStatus()).isEqualTo(ManpowerRequestStatus.PENDING_APPROVAL);

        verify(requestRepository, atLeastOnce()).save(any(ManpowerRequest.class));
    }

    @Test
    @DisplayName("Cập nhật trạng thái ManpowerRequest khi Workflow hoàn tất APPROVED")
    void testHandleWorkflowCompleted_Approved() {
        UUID requestId = UUID.randomUUID();
        UUID instanceId = UUID.randomUUID();

        ManpowerRequest mpr = ManpowerRequest.builder()
                .companyId(companyId)
                .status(ManpowerRequestStatus.PENDING_APPROVAL)
                .build();
        mpr.setId(requestId);

        when(requestRepository.findById(requestId)).thenReturn(Optional.of(mpr));

        recruitmentService.handleWorkflowCompleted(instanceId, requestId, ApprovalStatus.APPROVED, "Giám đốc đã duyệt");

        assertThat(mpr.getStatus()).isEqualTo(ManpowerRequestStatus.APPROVED);
        verify(requestRepository).save(mpr);
    }

    @Test
    @DisplayName("Nộp đánh giá phỏng vấn và phát hiện Divergence Alert khi ý kiến trái ngược")
    void testConsolidatedFeedback_DivergenceAlert() {
        UUID interviewId = UUID.randomUUID();
        Interview interview = Interview.builder()
                .companyId(companyId)
                .applicationId(UUID.randomUUID())
                .interviewType(InterviewType.TECHNICAL)
                .status(InterviewStatus.SCHEDULED)
                .build();
        interview.setId(interviewId);

        when(interviewRepository.findByIdAndCompanyId(interviewId, companyId)).thenReturn(Optional.of(interview));

        UUID interviewer1 = UUID.randomUUID();
        UUID interviewer2 = UUID.randomUUID();

        InterviewPanelMember m1 = InterviewPanelMember.builder().interviewerEmployeeId(interviewer1).build();
        InterviewPanelMember m2 = InterviewPanelMember.builder().interviewerEmployeeId(interviewer2).build();
        when(panelMemberRepository.findByInterviewId(interviewId)).thenReturn(List.of(m1, m2));

        InterviewEvaluation e1 = InterviewEvaluation.builder()
                .interviewId(interviewId)
                .interviewerEmployeeId(interviewer1)
                .overallRecommendation(InterviewRecommendation.STRONG_YES)
                .overallScore(BigDecimal.valueOf(4.8))
                .build();

        InterviewEvaluation e2 = InterviewEvaluation.builder()
                .interviewId(interviewId)
                .interviewerEmployeeId(interviewer2)
                .overallRecommendation(InterviewRecommendation.STRONG_NO)
                .overallScore(BigDecimal.valueOf(1.5))
                .build();

        when(evaluationRepository.findByInterviewId(interviewId)).thenReturn(List.of(e1, e2));
        when(employeeService.getEmployeeSummaries(any())).thenReturn(Collections.emptyMap());

        ConsolidatedFeedbackResponse feedback = interviewService.getConsolidatedFeedback(companyId, interviewId);

        assertThat(feedback).isNotNull();
        assertThat(feedback.isDivergenceAlert()).isTrue();
        assertThat(feedback.getDivergenceMessage()).contains("Cảnh báo chênh lệch bất thường");
        assertThat(feedback.getTotalPanelMembers()).isEqualTo(2);
        assertThat(feedback.getSubmittedEvaluations()).isEqualTo(2);
    }

    @Test
    @DisplayName("Tiếp nhận ứng viên trúng tuyển (Convert to Employee) thành công")
    void testConvertToEmployee_Success() {
        UUID applicationId = UUID.randomUUID();
        UUID candidateId = UUID.randomUUID();
        UUID jobPositionId = UUID.randomUUID();

        Application app = Application.builder()
                .companyId(companyId)
                .candidateId(candidateId)
                .jobPositionId(jobPositionId)
                .stage(ApplicationStage.OFFER_ACCEPTED)
                .build();
        app.setId(applicationId);

        Candidate candidate = Candidate.builder()
                .companyId(companyId)
                .fullName("Nguyen Van A")
                .email("nguyenvana@gmail.com")
                .phone("0987654321")
                .status(CandidateStatus.ACTIVE)
                .build();
        candidate.setId(candidateId);

        JobPosition jobPosition = JobPosition.builder()
                .companyId(companyId)
                .title("Senior Backend Engineer")
                .departmentId(departmentId)
                .positionId(positionId)
                .build();
        jobPosition.setId(jobPositionId);

        when(applicationRepository.findByIdAndCompanyId(applicationId, companyId)).thenReturn(Optional.of(app));
        when(candidateRepository.findById(candidateId)).thenReturn(Optional.of(candidate));
        when(jobPositionRepository.findById(jobPositionId)).thenReturn(Optional.of(jobPosition));

        EmployeeDetailResponse empDetail = EmployeeDetailResponse.builder()
                .id(UUID.randomUUID())
                .employeeCode("EMP-2026-0001")
                .fullName("Nguyen Van A")
                .companyEmail("nguyenvana@cyclosa.com")
                .hireDate(LocalDate.now())
                .build();

        when(employeeService.createEmployee(eq(companyId), any(CreateEmployeeRequest.class))).thenReturn(empDetail);

        ConvertToEmployeeRequest request = ConvertToEmployeeRequest.builder()
                .nationalIdNumber("001200001234")
                .companyEmail("nguyenvana@cyclosa.com")
                .build();

        ConvertToEmployeeResponse res = recruitmentService.convertToEmployee(companyId, applicationId, request);

        assertThat(res).isNotNull();
        assertThat(res.getEmployeeCode()).isEqualTo("EMP-2026-0001");
        assertThat(res.getFullName()).isEqualTo("Nguyen Van A");
        assertThat(app.getStage()).isEqualTo(ApplicationStage.HIRED);
        assertThat(candidate.getStatus()).isEqualTo(CandidateStatus.HIRED);

        verify(applicationRepository).save(app);
        verify(candidateRepository).save(candidate);
    }
}
