package com.cyclosa.discipline;

import com.cyclosa.common.dto.summary.CompanySummary;
import com.cyclosa.common.exception.AppException;
import com.cyclosa.common.security.SecurityPermissionEvaluator;
import com.cyclosa.discipline.dto.request.CreateDisciplineRequest;
import com.cyclosa.discipline.dto.request.CreateGrievanceRequest;
import com.cyclosa.discipline.dto.request.CreateRewardRequest;
import com.cyclosa.discipline.dto.response.DisciplineResponse;
import com.cyclosa.discipline.dto.response.GrievanceResponse;
import com.cyclosa.discipline.dto.response.RewardResponse;
import com.cyclosa.discipline.entity.Discipline;
import com.cyclosa.discipline.entity.Grievance;
import com.cyclosa.discipline.entity.Reward;
import com.cyclosa.discipline.enums.DisciplineStatus;
import com.cyclosa.discipline.enums.DisciplineType;
import com.cyclosa.discipline.enums.GrievanceCategory;
import com.cyclosa.discipline.enums.GrievanceStatus;
import com.cyclosa.discipline.enums.RewardType;
import com.cyclosa.discipline.enums.ViolationCategory;
import com.cyclosa.discipline.exception.DisciplineErrorCode;
import com.cyclosa.discipline.mapper.DisciplineMapper;
import com.cyclosa.discipline.repository.DisciplineRepository;
import com.cyclosa.discipline.repository.GrievanceRepository;
import com.cyclosa.discipline.repository.RewardRepository;
import com.cyclosa.discipline.service.RewardDisciplineService;
import com.cyclosa.employee.dto.response.EmployeeDetailResponse;
import com.cyclosa.employee.service.EmployeeService;
import com.cyclosa.organization.service.OrganizationalUnitService;
import com.cyclosa.discipline.dto.request.UpdateRewardRequest;
import com.cyclosa.discipline.dto.response.StatuteOfLimitationResponse;
import com.cyclosa.discipline.enums.DismissalGround;
import com.cyclosa.employee.dto.response.EmployeeDependentResponse;
import com.cyclosa.employee.enums.EmploymentStatus;
import com.cyclosa.employee.enums.FamilyRelationship;
import com.cyclosa.leave.service.LeaveRequestService;
import com.cyclosa.offboarding.dto.request.CreateTerminationRequest;
import com.cyclosa.offboarding.service.OffboardingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RewardDisciplineServiceTest {

    @Mock
    private RewardRepository rewardRepository;
    @Mock
    private DisciplineRepository disciplineRepository;
    @Mock
    private GrievanceRepository grievanceRepository;
    @Mock
    private EmployeeService employeeService;
    @Mock
    private LeaveRequestService leaveRequestService;
    @Mock
    private OffboardingService offboardingService;
    @Mock
    private OrganizationalUnitService orgUnitService;
    @Mock
    private SecurityPermissionEvaluator permEvaluator;
    @Spy
    private DisciplineMapper disciplineMapper = Mappers.getMapper(DisciplineMapper.class);

    @InjectMocks
    private RewardDisciplineService service;

    private UUID employeeId;
    private UUID companyId;
    private UUID userId;
    private EmployeeDetailResponse mockEmployee;

    @BeforeEach
    void setUp() {
        employeeId = UUID.randomUUID();
        companyId = UUID.randomUUID();
        userId = UUID.randomUUID();

        mockEmployee = EmployeeDetailResponse.builder()
                .id(employeeId)
                .fullName("Trần Văn B")
                .employeeCode("EMP001")
                .employmentStatus(EmploymentStatus.ACTIVE)
                .dependents(Collections.emptyList())
                .company(CompanySummary.builder().id(companyId).name("Cyclosa Corp").build())
                .build();
    }

    @Test
    @DisplayName("Create Reward: thành công khi hợp lệ")
    void createReward_Success() {
        CreateRewardRequest req = CreateRewardRequest.builder()
                .employeeId(employeeId)
                .rewardType(RewardType.MONETARY)
                .title("Khen thưởng nhân viên xuất sắc quý 1")
                .amount(new BigDecimal("5000000"))
                .reason("Vượt chỉ tiêu doanh số 150%")
                .decidedDate(LocalDate.now())
                .build();

        when(employeeService.getEmployeeByIdInternal(employeeId)).thenReturn(mockEmployee);
        when(rewardRepository.save(any(Reward.class))).thenAnswer(inv -> {
            Reward r = inv.getArgument(0);
            r.setId(UUID.randomUUID());
            return r;
        });

        RewardResponse res = service.createReward(req);

        assertThat(res).isNotNull();
        assertThat(res.getTitle()).isEqualTo(req.getTitle());
        assertThat(res.getAmount()).isEqualByComparingTo(new BigDecimal("5000000"));
        assertThat(res.getPushedToPayroll()).isFalse();
    }

    @Test
    @DisplayName("Delete Reward: chặn xóa nếu đã đẩy vào bảng lương")
    void deleteReward_AlreadyPushedToPayroll_ThrowsException() {
        UUID rewardId = UUID.randomUUID();
        Reward reward = Reward.builder().pushedToPayroll(true).build();
        reward.setId(rewardId);

        when(rewardRepository.findById(rewardId)).thenReturn(Optional.of(reward));

        assertThatThrownBy(() -> service.deleteReward(rewardId))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getErrorCode()).isEqualTo(DisciplineErrorCode.REWARD_ALREADY_PUSHED_TO_PAYROLL));
    }

    @Test
    @DisplayName("Create Discipline: chặn nếu đã quá thời hiệu xử lý theo Điều 123 BLLĐ")
    void createDiscipline_StatuteOfLimitationsExpired_ThrowsException() {
        CreateDisciplineRequest req = CreateDisciplineRequest.builder()
                .employeeId(employeeId)
                .violationDate(LocalDate.now().minusMonths(7)) // Quá 6 tháng đối với vi phạm GENERAL
                .violationCategory(ViolationCategory.GENERAL)
                .disciplineType(DisciplineType.KHIEN_TRACH)
                .build();

        when(employeeService.getEmployeeByIdInternal(employeeId)).thenReturn(mockEmployee);

        assertThatThrownBy(() -> service.createDiscipline(req))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getErrorCode()).isEqualTo(DisciplineErrorCode.STATUTE_OF_LIMITATIONS_EXPIRED));
    }

    @Test
    @DisplayName("Create Discipline: chặn sa thải nếu không có căn cứ Điều 125 BLLĐ")
    void createDiscipline_DismissalWithoutGround_ThrowsException() {
        CreateDisciplineRequest req = CreateDisciplineRequest.builder()
                .employeeId(employeeId)
                .violationDate(LocalDate.now().minusDays(10))
                .violationCategory(ViolationCategory.GENERAL)
                .disciplineType(DisciplineType.SA_THAI)
                .dismissalGround(null) // Thiếu căn cứ
                .build();

        when(employeeService.getEmployeeByIdInternal(employeeId)).thenReturn(mockEmployee);

        assertThatThrownBy(() -> service.createDiscipline(req))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getErrorCode()).isEqualTo(DisciplineErrorCode.DISMISSAL_REQUIRES_GROUND));
    }

    @Test
    @DisplayName("Decide Discipline: ban hành quyết định thành công và tính hạn xóa kỷ luật theo Điều 126")
    void decideDiscipline_Success_CalculatesExpiryDate() {
        UUID disciplineId = UUID.randomUUID();
        Discipline d = Discipline.builder()
                .employeeId(employeeId)
                .companyId(companyId)
                .violationDate(LocalDate.now().minusDays(5))
                .violationCategory(ViolationCategory.GENERAL)
                .statuteOfLimitationDeadline(LocalDate.now().plusMonths(5))
                .disciplineType(DisciplineType.KHIEN_TRACH)
                .status(DisciplineStatus.DRAFT)
                .handbookReference("Điều 10 Nội quy lao động")
                .evidenceFiles("evidence.pdf")
                .meetingDate(LocalDate.now().minusDays(1))
                .meetingAttendees("Ban Giám đốc, Đại diện Công đoàn, Người lao động")
                .build();
        d.setId(disciplineId);

        when(disciplineRepository.findById(disciplineId)).thenReturn(Optional.of(d));
        when(employeeService.findEmployeeIdByUserId(userId)).thenReturn(Optional.of(employeeId));
        when(disciplineRepository.save(any(Discipline.class))).thenAnswer(inv -> inv.getArgument(0));
        when(employeeService.getEmployeeByIdInternal(employeeId)).thenReturn(mockEmployee);

        DisciplineResponse res = service.decideDiscipline(disciplineId, userId);

        assertThat(res).isNotNull();
        assertThat(d.getStatus()).isEqualTo(DisciplineStatus.DECIDED);
        assertThat(d.getDecisionDate()).isEqualTo(LocalDate.now());
        // Khiển trách: hết hạn xóa kỷ luật sau 3 tháng
        assertThat(d.getExpiryDate()).isEqualTo(LocalDate.now().plusMonths(3));
    }

    @Test
    @DisplayName("Create Discipline: chặn xử lý kỷ luật nếu nhân viên đang nghỉ phép / nghỉ ốm theo Điều 122.4")
    void createDiscipline_WhenOnLeave_ThrowsException() {
        CreateDisciplineRequest req = CreateDisciplineRequest.builder()
                .employeeId(employeeId)
                .violationDate(LocalDate.now().minusDays(5))
                .violationCategory(ViolationCategory.GENERAL)
                .disciplineType(DisciplineType.KHIEN_TRACH)
                .build();

        EmployeeDetailResponse onLeaveEmp = EmployeeDetailResponse.builder()
                .id(employeeId)
                .fullName("Trần Văn B")
                .employmentStatus(EmploymentStatus.ON_LEAVE)
                .build();

        when(employeeService.getEmployeeByIdInternal(employeeId)).thenReturn(onLeaveEmp);

        assertThatThrownBy(() -> service.createDiscipline(req))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getErrorCode()).isEqualTo(DisciplineErrorCode.DISCIPLINE_RESTRICTED_BY_LAW));
    }

    @Test
    @DisplayName("Create Discipline: chặn xử lý kỷ luật nếu đang nuôi con dưới 12 tháng tuổi theo Điều 122.4")
    void createDiscipline_WhenNursingChildUnder12Months_ThrowsException() {
        CreateDisciplineRequest req = CreateDisciplineRequest.builder()
                .employeeId(employeeId)
                .violationDate(LocalDate.now().minusDays(5))
                .violationCategory(ViolationCategory.GENERAL)
                .disciplineType(DisciplineType.KHIEN_TRACH)
                .build();

        EmployeeDependentResponse baby = EmployeeDependentResponse.builder()
                .relationship(FamilyRelationship.CHILD)
                .dateOfBirth(LocalDate.now().minusMonths(6))
                .build();

        EmployeeDetailResponse empWithBaby = EmployeeDetailResponse.builder()
                .id(employeeId)
                .fullName("Trần Thị C")
                .employmentStatus(EmploymentStatus.ACTIVE)
                .dependents(List.of(baby))
                .build();

        when(employeeService.getEmployeeByIdInternal(employeeId)).thenReturn(empWithBaby);

        assertThatThrownBy(() -> service.createDiscipline(req))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getErrorCode()).isEqualTo(DisciplineErrorCode.DISCIPLINE_RESTRICTED_BY_LAW));
    }

    @Test
    @DisplayName("Create Discipline: chặn sa thải trực tiếp trạng thái DECIDED nếu chưa qua Workflow phê duyệt")
    void createDiscipline_DismissalWithDecidedStatus_ThrowsException() {
        CreateDisciplineRequest req = CreateDisciplineRequest.builder()
                .employeeId(employeeId)
                .violationDate(LocalDate.now().minusDays(5))
                .violationCategory(ViolationCategory.GENERAL)
                .disciplineType(DisciplineType.SA_THAI)
                .dismissalGround(DismissalGround.THEFT_FRAUD_GAMBLING_ASSAULT_DRUGS)
                .status(DisciplineStatus.DECIDED)
                .build();

        when(employeeService.getEmployeeByIdInternal(employeeId)).thenReturn(mockEmployee);

        assertThatThrownBy(() -> service.createDiscipline(req))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getErrorCode()).isEqualTo(DisciplineErrorCode.TERMINATION_REQUIRES_APPROVAL));
    }

    @Test
    @DisplayName("Decide Discipline: chặn quyết định sa thải khi gọi trực tiếp decideDiscipline")
    void decideDiscipline_WhenDismissal_ThrowsTerminationRequiresApproval() {
        UUID disciplineId = UUID.randomUUID();
        Discipline d = Discipline.builder()
                .employeeId(employeeId)
                .companyId(companyId)
                .violationDate(LocalDate.now().minusDays(5))
                .violationCategory(ViolationCategory.GENERAL)
                .statuteOfLimitationDeadline(LocalDate.now().plusMonths(5))
                .disciplineType(DisciplineType.SA_THAI)
                .status(DisciplineStatus.DRAFT)
                .handbookReference("Điều 10 Nội quy")
                .evidenceFiles("evidence.pdf")
                .meetingDate(LocalDate.now().minusDays(1))
                .meetingAttendees("BGD, Công đoàn, NLĐ")
                .build();
        d.setId(disciplineId);

        when(disciplineRepository.findById(disciplineId)).thenReturn(Optional.of(d));

        assertThatThrownBy(() -> service.decideDiscipline(disciplineId, userId))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getErrorCode()).isEqualTo(DisciplineErrorCode.TERMINATION_REQUIRES_APPROVAL));
    }

    @Test
    @DisplayName("Approve Dismissal: phê duyệt sa thải thành công và tự động tạo Termination sang Offboarding")
    void approveDismissal_Success_CreatesTermination() {
        UUID disciplineId = UUID.randomUUID();
        Discipline d = Discipline.builder()
                .employeeId(employeeId)
                .companyId(companyId)
                .violationDate(LocalDate.now().minusDays(5))
                .violationCategory(ViolationCategory.GENERAL)
                .statuteOfLimitationDeadline(LocalDate.now().plusMonths(5))
                .disciplineType(DisciplineType.SA_THAI)
                .status(DisciplineStatus.DRAFT)
                .handbookReference("Điều 10 Nội quy")
                .evidenceFiles("evidence.pdf")
                .meetingDate(LocalDate.now().minusDays(1))
                .meetingAttendees("BGD, Công đoàn, NLĐ")
                .build();
        d.setId(disciplineId);

        when(disciplineRepository.findById(disciplineId)).thenReturn(Optional.of(d));
        when(employeeService.findEmployeeIdByUserId(userId)).thenReturn(Optional.of(employeeId));
        when(disciplineRepository.save(any(Discipline.class))).thenAnswer(inv -> inv.getArgument(0));
        when(employeeService.getEmployeeByIdInternal(employeeId)).thenReturn(mockEmployee);

        DisciplineResponse res = service.approveDismissal(disciplineId, userId);

        assertThat(res).isNotNull();
        assertThat(d.getStatus()).isEqualTo(DisciplineStatus.DECIDED);
        verify(offboardingService, times(1)).createTermination(any(CreateTerminationRequest.class), eq(userId));
    }

    @Test
    @DisplayName("Check Statute of Limitation: tra cứu thời hiệu theo Điều 123 BLLĐ thành công")
    void checkStatuteOfLimitation_Success() {
        UUID disciplineId = UUID.randomUUID();
        Discipline d = Discipline.builder()
                .employeeId(employeeId)
                .companyId(companyId)
                .violationDate(LocalDate.now().minusDays(30))
                .violationCategory(ViolationCategory.GENERAL)
                .statuteOfLimitationDeadline(LocalDate.now().plusDays(150))
                .build();
        d.setId(disciplineId);

        when(disciplineRepository.findById(disciplineId)).thenReturn(Optional.of(d));

        StatuteOfLimitationResponse res = service.checkStatuteOfLimitation(disciplineId);

        assertThat(res).isNotNull();
        assertThat(res.isExpired()).isFalse();
        assertThat(res.getRemainingDays()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Auto Expire Disciplines: cập nhật EXPIRED cho các hồ sơ đến ngày xóa kỷ luật")
    void autoExpireDisciplines_Success() {
        Discipline d = Discipline.builder()
                .employeeId(employeeId)
                .status(DisciplineStatus.DECIDED)
                .expiryDate(LocalDate.now().minusDays(1))
                .build();

        when(disciplineRepository.findByStatusAndExpiryDateLessThanEqual(eq(DisciplineStatus.DECIDED), any(LocalDate.class)))
                .thenReturn(List.of(d));

        int count = service.autoExpireDisciplines();

        assertThat(count).isEqualTo(1);
        assertThat(d.getStatus()).isEqualTo(DisciplineStatus.EXPIRED);
        verify(disciplineRepository).save(d);
    }

    @Test
    @DisplayName("Update Reward: cập nhật khen thưởng thành công khi chưa đẩy vào bảng lương")
    void updateReward_Success() {
        UUID rewardId = UUID.randomUUID();
        Reward r = Reward.builder()
                .employeeId(employeeId)
                .title("Khen thưởng cũ")
                .amount(new BigDecimal("1000000"))
                .pushedToPayroll(false)
                .build();
        r.setId(rewardId);

        UpdateRewardRequest req = UpdateRewardRequest.builder()
                .title("Khen thưởng mới")
                .amount(new BigDecimal("2000000"))
                .build();

        when(rewardRepository.findById(rewardId)).thenReturn(Optional.of(r));
        when(rewardRepository.save(any(Reward.class))).thenAnswer(inv -> inv.getArgument(0));
        when(employeeService.getEmployeeByIdInternal(employeeId)).thenReturn(mockEmployee);

        RewardResponse res = service.updateReward(rewardId, req);

        assertThat(res).isNotNull();
        assertThat(r.getTitle()).isEqualTo("Khen thưởng mới");
        assertThat(r.getAmount()).isEqualByComparingTo(new BigDecimal("2000000"));
    }

    @Test
    @DisplayName("Create Grievance: nộp đơn khiếu nại thành công")
    void createGrievance_Success() {
        CreateGrievanceRequest req = CreateGrievanceRequest.builder()
                .category(GrievanceCategory.COMPENSATION)
                .description("Sai lệch số giờ làm thêm trong bảng chấm công")
                .build();

        when(employeeService.findEmployeeIdByUserId(userId)).thenReturn(Optional.of(employeeId));
        when(employeeService.getEmployeeByIdInternal(employeeId)).thenReturn(mockEmployee);
        when(grievanceRepository.save(any(Grievance.class))).thenAnswer(inv -> {
            Grievance g = inv.getArgument(0);
            g.setId(UUID.randomUUID());
            return g;
        });

        GrievanceResponse res = service.createGrievance(req, userId);

        assertThat(res).isNotNull();
        assertThat(res.getStatus()).isEqualTo(GrievanceStatus.OPEN);
        assertThat(res.getCategory()).isEqualTo(GrievanceCategory.COMPENSATION);
    }
}
