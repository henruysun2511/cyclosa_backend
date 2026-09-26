package com.cyclosa.talent;

import com.cyclosa.common.dto.summary.EmployeeSummary;
import com.cyclosa.common.dto.summary.PositionSummary;
import com.cyclosa.common.exception.AppException;
import com.cyclosa.employee.dto.response.EmployeeDetailResponse;
import com.cyclosa.employee.service.EmployeeService;
import com.cyclosa.organization.dto.response.PositionDetailResponse;
import com.cyclosa.organization.service.PositionService;
import com.cyclosa.talent.dto.request.AddSuccessionCandidateRequest;
import com.cyclosa.talent.dto.request.CreateSuccessionPlanRequest;
import com.cyclosa.talent.dto.response.SuccessionCandidateResponse;
import com.cyclosa.talent.dto.response.SuccessionPlanDetailResponse;
import com.cyclosa.talent.dto.response.SuccessionPlanResponse;
import com.cyclosa.talent.entity.SuccessionCandidate;
import com.cyclosa.talent.entity.SuccessionPlan;
import com.cyclosa.talent.enums.SuccessionReadiness;
import com.cyclosa.talent.enums.SuccessionRisk;
import com.cyclosa.talent.exception.TalentErrorCode;
import com.cyclosa.talent.mapper.TalentMapper;
import com.cyclosa.talent.repository.SuccessionCandidateRepository;
import com.cyclosa.talent.repository.SuccessionPlanRepository;
import com.cyclosa.talent.service.SuccessionPlanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SuccessionPlanServiceTest {

    @Mock
    private SuccessionPlanRepository successionPlanRepository;

    @Mock
    private SuccessionCandidateRepository successionCandidateRepository;

    @Mock
    private PositionService positionService;

    @Mock
    private EmployeeService employeeService;

    @Spy
    private TalentMapper talentMapper = Mappers.getMapper(TalentMapper.class);

    @InjectMocks
    private SuccessionPlanService successionPlanService;

    private UUID companyId;
    private UUID positionId;
    private UUID planId;
    private SuccessionPlan samplePlan;

    @BeforeEach
    void setUp() {
        companyId = UUID.randomUUID();
        positionId = UUID.randomUUID();
        planId = UUID.randomUUID();

        samplePlan = SuccessionPlan.builder()
                .companyId(companyId)
                .positionId(positionId)
                .riskLevel(SuccessionRisk.HIGH)
                .reviewDate(LocalDate.now().plusMonths(6))
                .candidates(new ArrayList<>())
                .build();
        samplePlan.setId(planId);
    }

    @Test
    @DisplayName("Lập kế hoạch kế nhiệm thành công")
    void createSuccessionPlan_Success() {
        CreateSuccessionPlanRequest request = CreateSuccessionPlanRequest.builder()
                .positionId(positionId)
                .riskLevel(SuccessionRisk.HIGH)
                .reviewDate(LocalDate.now().plusMonths(6))
                .build();

        when(positionService.getPositionById(companyId, positionId)).thenReturn(PositionDetailResponse.builder().id(positionId).build());
        when(successionPlanRepository.existsByCompanyIdAndPositionId(companyId, positionId)).thenReturn(false);
        when(successionPlanRepository.save(any(SuccessionPlan.class))).thenReturn(samplePlan);
        when(positionService.getPositionSummary(positionId)).thenReturn(PositionSummary.builder().id(positionId).name("Tech Lead").build());

        SuccessionPlanResponse response = successionPlanService.createSuccessionPlan(companyId, request);

        assertThat(response).isNotNull();
        assertThat(response.getPositionId()).isEqualTo(positionId);
        assertThat(response.getRiskLevel()).isEqualTo(SuccessionRisk.HIGH);
        assertThat(response.getPosition().getName()).isEqualTo("Tech Lead");
    }

    @Test
    @DisplayName("Lập kế hoạch kế nhiệm thất bại nếu vị trí đã có kế hoạch")
    void createSuccessionPlan_Duplicate_ThrowsException() {
        CreateSuccessionPlanRequest request = CreateSuccessionPlanRequest.builder()
                .positionId(positionId)
                .riskLevel(SuccessionRisk.HIGH)
                .build();

        when(positionService.getPositionById(companyId, positionId)).thenReturn(PositionDetailResponse.builder().id(positionId).build());
        when(successionPlanRepository.existsByCompanyIdAndPositionId(companyId, positionId)).thenReturn(true);

        assertThatThrownBy(() -> successionPlanService.createSuccessionPlan(companyId, request))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(TalentErrorCode.SUCCESSION_PLAN_ALREADY_EXISTS));
    }

    @Test
    @DisplayName("Thêm ứng viên kế nhiệm thành công")
    void addCandidate_Success() {
        UUID candidateEmpId = UUID.randomUUID();
        UUID otherPositionId = UUID.randomUUID();

        AddSuccessionCandidateRequest request = AddSuccessionCandidateRequest.builder()
                .employeeId(candidateEmpId)
                .readiness(SuccessionReadiness.READY_NOW)
                .note("Nhân sự xuất sắc đã sẵn sàng nhận nhiệm vụ")
                .build();

        when(successionPlanRepository.findByIdAndCompanyId(planId, companyId)).thenReturn(Optional.of(samplePlan));
        when(employeeService.getEmployeeById(companyId, candidateEmpId)).thenReturn(
                EmployeeDetailResponse.builder()
                        .id(candidateEmpId)
                        .position(PositionSummary.builder().id(otherPositionId).name("Senior Dev").build())
                        .build()
        );
        when(successionCandidateRepository.existsBySuccessionPlanIdAndEmployeeId(planId, candidateEmpId)).thenReturn(false);

        SuccessionCandidate savedCandidate = SuccessionCandidate.builder()
                .successionPlan(samplePlan)
                .companyId(companyId)
                .employeeId(candidateEmpId)
                .readiness(SuccessionReadiness.READY_NOW)
                .note("Nhân sự xuất sắc đã sẵn sàng nhận nhiệm vụ")
                .build();
        savedCandidate.setId(UUID.randomUUID());

        when(successionCandidateRepository.save(any(SuccessionCandidate.class))).thenReturn(savedCandidate);
        when(employeeService.getEmployeeSummaries(Set.of(candidateEmpId))).thenReturn(Map.of(
                candidateEmpId, EmployeeSummary.builder().id(candidateEmpId).fullName("Nguyễn Văn B").build()
        ));

        SuccessionCandidateResponse response = successionPlanService.addCandidate(companyId, planId, request);

        assertThat(response).isNotNull();
        assertThat(response.getEmployeeId()).isEqualTo(candidateEmpId);
        assertThat(response.getReadiness()).isEqualTo(SuccessionReadiness.READY_NOW);
        assertThat(response.getEmployee().getFullName()).isEqualTo("Nguyễn Văn B");
    }

    @Test
    @DisplayName("Thêm ứng viên thất bại nếu ứng viên đang giữ chính vị trí của plan (Rule 15)")
    void addCandidate_CurrentHolder_ThrowsException() {
        UUID currentHolderEmpId = UUID.randomUUID();

        AddSuccessionCandidateRequest request = AddSuccessionCandidateRequest.builder()
                .employeeId(currentHolderEmpId)
                .readiness(SuccessionReadiness.READY_NOW)
                .build();

        when(successionPlanRepository.findByIdAndCompanyId(planId, companyId)).thenReturn(Optional.of(samplePlan));
        when(employeeService.getEmployeeById(companyId, currentHolderEmpId)).thenReturn(
                EmployeeDetailResponse.builder()
                        .id(currentHolderEmpId)
                        .position(PositionSummary.builder().id(positionId).name("Tech Lead").build())
                        .build()
        );

        assertThatThrownBy(() -> successionPlanService.addCandidate(companyId, planId, request))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(TalentErrorCode.CANDIDATE_IS_CURRENT_HOLDER));
    }

    @Test
    @DisplayName("Thêm ứng viên thất bại nếu ứng viên đã có trong plan (Rule 15)")
    void addCandidate_AlreadyAdded_ThrowsException() {
        UUID candidateEmpId = UUID.randomUUID();
        UUID otherPositionId = UUID.randomUUID();

        AddSuccessionCandidateRequest request = AddSuccessionCandidateRequest.builder()
                .employeeId(candidateEmpId)
                .readiness(SuccessionReadiness.READY_1_2_YEARS)
                .build();

        when(successionPlanRepository.findByIdAndCompanyId(planId, companyId)).thenReturn(Optional.of(samplePlan));
        when(employeeService.getEmployeeById(companyId, candidateEmpId)).thenReturn(
                EmployeeDetailResponse.builder()
                        .id(candidateEmpId)
                        .position(PositionSummary.builder().id(otherPositionId).build())
                        .build()
        );
        when(successionCandidateRepository.existsBySuccessionPlanIdAndEmployeeId(planId, candidateEmpId)).thenReturn(true);

        assertThatThrownBy(() -> successionPlanService.addCandidate(companyId, planId, request))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(TalentErrorCode.CANDIDATE_ALREADY_ADDED));
    }
}
