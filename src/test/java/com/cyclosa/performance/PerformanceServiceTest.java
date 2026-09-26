package com.cyclosa.performance;

import com.cyclosa.common.dto.summary.CompanySummary;
import com.cyclosa.common.dto.summary.EmployeeSummary;
import com.cyclosa.common.dto.summary.OrgUnitSummary;
import com.cyclosa.common.dto.summary.PositionSummary;
import com.cyclosa.common.exception.AppException;
import com.cyclosa.common.security.SecurityPermissionEvaluator;
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
import com.cyclosa.performance.service.PerformanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PerformanceServiceTest {

    @Mock
    private PerformanceCycleRepository cycleRepository;
    @Mock
    private KpiRepository kpiRepository;
    @Mock
    private GoalRepository goalRepository;
    @Mock
    private PerformanceEvaluationRepository evaluationRepository;
    @Mock
    private PerformanceReviewRepository reviewRepository;

    @Mock
    private EmployeeService employeeService;
    @Mock
    private OrganizationalUnitService orgUnitService;
    @Mock
    private PositionService positionService;
    @Mock
    private CompanyService companyService;
    @Mock
    private SecurityPermissionEvaluator permEvaluator;
    @Mock
    private PerformanceMapper performanceMapper;

    @InjectMocks
    private PerformanceService performanceService;

    private UUID companyId;
    private UUID employeeId;
    private UUID userId;
    private UUID cycleId;

    @BeforeEach
    void setUp() {
        companyId = UUID.randomUUID();
        employeeId = UUID.randomUUID();
        userId = UUID.randomUUID();
        cycleId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Create PerformanceCycle: thành công khi ngày hợp lệ")
    void createCycle_Success() {
        CreatePerformanceCycleRequest req = CreatePerformanceCycleRequest.builder()
                .name("Kỳ đánh giá Q1 2026")
                .cycleType(PerformanceCycleType.QUARTERLY)
                .startDate(LocalDate.of(2026, 1, 1))
                .endDate(LocalDate.of(2026, 3, 31))
                .companyId(companyId)
                .description("Đánh giá hiệu suất Q1")
                .build();

        PerformanceCycle cycleEntity = PerformanceCycle.builder()
                .name(req.getName())
                .cycleType(req.getCycleType())
                .startDate(req.getStartDate())
                .endDate(req.getEndDate())
                .companyId(companyId)
                .build();

        PerformanceCycleResponse expectedResponse = PerformanceCycleResponse.builder()
                .id(cycleId)
                .name(req.getName())
                .status(PerformanceCycleStatus.DRAFT)
                .build();

        when(cycleRepository.existsByNameAndCompanyId(req.getName(), companyId)).thenReturn(false);
        when(performanceMapper.toEntity(req)).thenReturn(cycleEntity);
        when(cycleRepository.save(any(PerformanceCycle.class))).thenAnswer(inv -> {
            PerformanceCycle c = inv.getArgument(0);
            c.setId(cycleId);
            return c;
        });
        when(performanceMapper.toResponse(any(PerformanceCycle.class))).thenReturn(expectedResponse);

        PerformanceCycleResponse res = performanceService.createCycle(req);

        assertThat(res).isNotNull();
        assertThat(res.getId()).isEqualTo(cycleId);
        assertThat(res.getName()).isEqualTo(req.getName());
        assertThat(res.getStatus()).isEqualTo(PerformanceCycleStatus.DRAFT);
    }

    @Test
    @DisplayName("Create PerformanceCycle: ném lỗi nếu ngày bắt đầu sau ngày kết thúc")
    void createCycle_InvalidDates_ThrowsException() {
        CreatePerformanceCycleRequest req = CreatePerformanceCycleRequest.builder()
                .name("Kỳ lỗi ngày")
                .cycleType(PerformanceCycleType.QUARTERLY)
                .startDate(LocalDate.of(2026, 4, 1))
                .endDate(LocalDate.of(2026, 3, 31))
                .companyId(companyId)
                .build();

        assertThatThrownBy(() -> performanceService.createCycle(req))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Ngày bắt đầu không được sau ngày kết thúc");
    }

    @Test
    @DisplayName("Create Goal: kiểm tra chặn nếu chu kỳ không ACTIVE")
    void createGoal_CycleNotActive_ThrowsException() {
        CreateGoalRequest req = CreateGoalRequest.builder()
                .employeeId(employeeId)
                .performanceCycleId(cycleId)
                .title("Tăng doanh số")
                .targetValue("500000000")
                .weightPercentage(new BigDecimal("30.00"))
                .build();

        EmployeeDetailResponse empDetail = EmployeeDetailResponse.builder()
                .id(employeeId)
                .fullName("Nguyễn Văn A")
                .build();
        when(employeeService.getEmployeeByIdInternal(employeeId)).thenReturn(empDetail);

        PerformanceCycle cycle = PerformanceCycle.builder().status(PerformanceCycleStatus.DRAFT).build();
        cycle.setId(cycleId);
        when(cycleRepository.findById(cycleId)).thenReturn(Optional.of(cycle));

        assertThatThrownBy(() -> performanceService.createGoal(req))
                .isInstanceOf(AppException.class);
    }

    @Test
    @DisplayName("Create Goal: kiểm tra chặn tổng trọng số vượt quá 100%")
    void createGoal_WeightExceeds100_ThrowsException() {
        CreateGoalRequest req = CreateGoalRequest.builder()
                .employeeId(employeeId)
                .performanceCycleId(cycleId)
                .title("Mục tiêu vượt mức")
                .targetValue("100")
                .weightPercentage(new BigDecimal("40.00"))
                .build();

        EmployeeDetailResponse empDetail = EmployeeDetailResponse.builder()
                .id(employeeId)
                .fullName("Nguyễn Văn A")
                .build();
        when(employeeService.getEmployeeByIdInternal(employeeId)).thenReturn(empDetail);

        PerformanceCycle cycle = PerformanceCycle.builder().status(PerformanceCycleStatus.ACTIVE).build();
        cycle.setId(cycleId);
        when(cycleRepository.findById(cycleId)).thenReturn(Optional.of(cycle));

        // Đã có sẵn 70% trọng số
        when(goalRepository.sumWeightByEmployeeAndCycle(employeeId, cycleId, null))
                .thenReturn(new BigDecimal("70.00"));

        assertThatThrownBy(() -> performanceService.createGoal(req))
                .isInstanceOf(AppException.class);
    }

    @Test
    @DisplayName("Submit Self-Review: nộp điểm tự đánh giá thành công")
    void submitSelfReview_Success() {
        UUID evalId = UUID.randomUUID();
        UUID goalId = UUID.randomUUID();

        PerformanceEvaluation eval = PerformanceEvaluation.builder()
                .employeeId(employeeId)
                .performanceCycleId(cycleId)
                .status(EvaluationStatus.DRAFT)
                .build();
        eval.setId(evalId);

        when(evaluationRepository.findById(evalId)).thenReturn(Optional.of(eval));
        when(employeeService.findEmployeeIdByUserId(userId)).thenReturn(Optional.of(employeeId));

        Goal goal = Goal.builder().employeeId(employeeId).performanceCycleId(cycleId).build();
        goal.setId(goalId);
        when(goalRepository.findById(goalId)).thenReturn(Optional.of(goal));

        when(reviewRepository.findByGoalIdAndReviewType(goalId, ReviewType.SELF)).thenReturn(Optional.empty());
        when(evaluationRepository.save(any(PerformanceEvaluation.class))).thenAnswer(inv -> inv.getArgument(0));

        SubmitSelfReviewRequest req = SubmitSelfReviewRequest.builder()
                .reviews(List.of(GoalReviewItemRequest.builder()
                        .goalId(goalId)
                        .score(new BigDecimal("95.00"))
                        .comment("Đạt chỉ tiêu đề ra")
                        .build()))
                .selfOverallComment("Tôi đã hoàn thành tốt mục tiêu")
                .build();

        PerformanceEvaluationDetailResponse res = performanceService.submitSelfReview(evalId, req, userId);

        assertThat(res).isNotNull();
        assertThat(eval.getStatus()).isEqualTo(EvaluationStatus.SELF_REVIEWED);
        verify(reviewRepository).save(any(PerformanceReview.class));
    }

    @Test
    @DisplayName("Submit Manager-Review: tính toán điểm bình quân gia quyền và xếp loại tự động")
    void submitManagerReview_CalculatesWeightedScoreAndRating() {
        UUID evalId = UUID.randomUUID();
        UUID goal1Id = UUID.randomUUID();
        UUID goal2Id = UUID.randomUUID();

        PerformanceEvaluation eval = PerformanceEvaluation.builder()
                .employeeId(employeeId)
                .performanceCycleId(cycleId)
                .status(EvaluationStatus.SELF_REVIEWED)
                .build();
        eval.setId(evalId);

        when(evaluationRepository.findById(evalId)).thenReturn(Optional.of(eval));
        when(employeeService.findEmployeeIdByUserId(userId)).thenReturn(Optional.of(UUID.randomUUID()));

        Goal goal1 = Goal.builder().employeeId(employeeId).performanceCycleId(cycleId).weightPercentage(new BigDecimal("60.00")).build();
        goal1.setId(goal1Id);
        Goal goal2 = Goal.builder().employeeId(employeeId).performanceCycleId(cycleId).weightPercentage(new BigDecimal("40.00")).build();
        goal2.setId(goal2Id);

        when(goalRepository.findById(goal1Id)).thenReturn(Optional.of(goal1));
        when(goalRepository.findById(goal2Id)).thenReturn(Optional.of(goal2));

        PerformanceReview r1 = PerformanceReview.builder().goalId(goal1Id).reviewType(ReviewType.MANAGER).score(new BigDecimal("90.00")).build();
        PerformanceReview r2 = PerformanceReview.builder().goalId(goal2Id).reviewType(ReviewType.MANAGER).score(new BigDecimal("80.00")).build();

        when(reviewRepository.findByGoalIdAndReviewType(goal1Id, ReviewType.MANAGER)).thenReturn(Optional.of(r1));
        when(reviewRepository.findByGoalIdAndReviewType(goal2Id, ReviewType.MANAGER)).thenReturn(Optional.of(r2));
        when(goalRepository.findByEmployeeIdAndPerformanceCycleId(employeeId, cycleId)).thenReturn(List.of(goal1, goal2));
        when(evaluationRepository.save(any(PerformanceEvaluation.class))).thenAnswer(inv -> inv.getArgument(0));

        SubmitManagerReviewRequest req = SubmitManagerReviewRequest.builder()
                .reviews(List.of(
                        GoalReviewItemRequest.builder().goalId(goal1Id).score(new BigDecimal("90.00")).build(),
                        GoalReviewItemRequest.builder().goalId(goal2Id).score(new BigDecimal("80.00")).build()
                ))
                .managerOverallComment("Làm việc tích cực")
                .build();

        PerformanceEvaluationDetailResponse res = performanceService.submitManagerReview(evalId, req, userId);

        assertThat(res).isNotNull();
        assertThat(eval.getFinalScore()).isEqualByComparingTo(new BigDecimal("86.00"));
        assertThat(eval.getRating()).isEqualTo(PerformanceRating.GOOD);
        assertThat(eval.getStatus()).isEqualTo(EvaluationStatus.MANAGER_REVIEWED);
    }

    @Test
    @DisplayName("Finalize Evaluation: chặn chốt điểm lần 2 nếu đã FINALIZED")
    void finalizeEvaluation_AlreadyFinalized_ThrowsException() {
        UUID evalId = UUID.randomUUID();

        PerformanceEvaluation eval = PerformanceEvaluation.builder()
                .employeeId(employeeId)
                .performanceCycleId(cycleId)
                .status(EvaluationStatus.FINALIZED)
                .build();
        eval.setId(evalId);

        when(evaluationRepository.findById(evalId)).thenReturn(Optional.of(eval));

        FinalizeEvaluationRequest req = FinalizeEvaluationRequest.builder()
                .finalScore(new BigDecimal("85.00"))
                .build();

        assertThatThrownBy(() -> performanceService.finalizeEvaluation(evalId, req, userId))
                .isInstanceOf(AppException.class);
    }
}
