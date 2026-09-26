package com.cyclosa.offboarding;

import com.cyclosa.asset.service.AssetService;
import com.cyclosa.auth.service.UserService;
import com.cyclosa.common.dto.summary.CompanySummary;
import com.cyclosa.common.enums.UserStatus;
import com.cyclosa.common.exception.AppException;
import com.cyclosa.common.security.SecurityPermissionEvaluator;
import com.cyclosa.contract.dto.response.ContractResponse;
import com.cyclosa.contract.enums.ContractType;
import com.cyclosa.contract.service.ContractService;
import com.cyclosa.employee.dto.response.EmployeeDetailResponse;
import com.cyclosa.employee.enums.EmploymentStatus;
import com.cyclosa.employee.service.EmployeeService;
import com.cyclosa.leave.service.LeaveBalanceService;
import com.cyclosa.offboarding.dto.request.ApplyResignationRequest;
import com.cyclosa.offboarding.dto.request.ApproveResignationRequest;
import com.cyclosa.offboarding.dto.request.UpdateClearanceRequest;
import com.cyclosa.offboarding.dto.response.OffboardingSummaryResponse;
import com.cyclosa.offboarding.dto.response.ResignationResponse;
import com.cyclosa.offboarding.entity.OffboardingClearance;
import com.cyclosa.offboarding.entity.Resignation;
import com.cyclosa.offboarding.entity.Termination;
import com.cyclosa.offboarding.enums.ClearanceStatus;
import com.cyclosa.offboarding.enums.ClearanceType;
import com.cyclosa.offboarding.enums.ResignationReason;
import com.cyclosa.offboarding.enums.ResignationStatus;
import com.cyclosa.offboarding.enums.TerminationStatus;
import com.cyclosa.offboarding.exception.OffboardingErrorCode;
import com.cyclosa.offboarding.mapper.OffboardingMapper;
import com.cyclosa.offboarding.repository.ExitInterviewRepository;
import com.cyclosa.offboarding.repository.OffboardingClearanceRepository;
import com.cyclosa.offboarding.repository.ResignationRepository;
import com.cyclosa.offboarding.repository.TerminationRepository;
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
class OffboardingServiceTest {

    @Mock
    private ResignationRepository resignationRepository;
    @Mock
    private TerminationRepository terminationRepository;
    @Mock
    private ExitInterviewRepository exitInterviewRepository;
    @Mock
    private OffboardingClearanceRepository clearanceRepository;
    @Mock
    private EmployeeService employeeService;
    @Mock
    private AssetService assetService;
    @Mock
    private LeaveBalanceService leaveBalanceService;
    @Mock
    private UserService userService;
    @Mock
    private SecurityPermissionEvaluator permEvaluator;
    @Mock
    private ContractService contractService;
    @Spy
    private OffboardingMapper offboardingMapper = Mappers.getMapper(OffboardingMapper.class);

    @InjectMocks
    private OffboardingService service;

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
                .fullName("Hoàng Văn E")
                .employeeCode("EMP005")
                .employmentStatus(EmploymentStatus.ACTIVE)
                .company(CompanySummary.builder().id(companyId).name("Cyclosa Corp").build())
                .build();
    }

    @Test
    @DisplayName("Apply Resignation: nộp đơn xin thôi việc thành công và KHÔNG khởi tạo clearance sớm")
    void applyResignation_Success() {
        ApplyResignationRequest req = ApplyResignationRequest.builder()
                .expectedLastWorkingDate(LocalDate.now().plusDays(35))
                .personalReasonCategory(ResignationReason.BETTER_OPPORTUNITY)
                .reasonDetail("Tìm được cơ hội phát triển mới")
                .build();

        when(employeeService.findEmployeeIdByUserId(userId)).thenReturn(Optional.of(employeeId));
        when(employeeService.getEmployeeByIdInternal(employeeId)).thenReturn(mockEmployee);
        when(resignationRepository.existsByEmployeeIdAndStatusIn(eq(employeeId), any())).thenReturn(false);
        when(terminationRepository.existsByEmployeeIdAndStatusIn(eq(employeeId), any())).thenReturn(false);

        ContractResponse contract = ContractResponse.builder()
                .contractType(ContractType.DEFINITE_TERM)
                .build();
        when(contractService.getActiveContractByEmployee(employeeId)).thenReturn(Optional.of(contract));

        when(resignationRepository.save(any(Resignation.class))).thenAnswer(inv -> {
            Resignation r = inv.getArgument(0);
            r.setId(UUID.randomUUID());
            return r;
        });

        ResignationResponse res = service.applyResignation(req, userId);

        assertThat(res).isNotNull();
        assertThat(res.getStatus()).isEqualTo(ResignationStatus.PENDING);
        assertThat(res.getPersonalReasonCategory()).isEqualTo(ResignationReason.BETTER_OPPORTUNITY);
        // Clearance KHÔNG được lưu khi mới nộp đơn PENDING
        verify(clearanceRepository, never()).save(any(OffboardingClearance.class));
    }

    @Test
    @DisplayName("Apply Resignation: báo trước không đủ số ngày theo quy định BLLD -> ném lỗi 422")
    void applyResignation_InsufficientNotice_ThrowsException() {
        ApplyResignationRequest req = ApplyResignationRequest.builder()
                .expectedLastWorkingDate(LocalDate.now().plusDays(10)) // Chỉ báo trước 10 ngày
                .personalReasonCategory(ResignationReason.BETTER_OPPORTUNITY)
                .build();

        when(employeeService.findEmployeeIdByUserId(userId)).thenReturn(Optional.of(employeeId));
        when(employeeService.getEmployeeByIdInternal(employeeId)).thenReturn(mockEmployee);
        when(resignationRepository.existsByEmployeeIdAndStatusIn(eq(employeeId), any())).thenReturn(false);
        when(terminationRepository.existsByEmployeeIdAndStatusIn(eq(employeeId), any())).thenReturn(false);

        ContractResponse contract = ContractResponse.builder()
                .contractType(ContractType.DEFINITE_TERM) // Cần tối thiểu 30 ngày
                .build();
        when(contractService.getActiveContractByEmployee(employeeId)).thenReturn(Optional.of(contract));

        assertThatThrownBy(() -> service.applyResignation(req, userId))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getErrorCode()).isEqualTo(OffboardingErrorCode.INSUFFICIENT_NOTICE_PERIOD));
    }

    @Test
    @DisplayName("Apply Resignation: đã có đơn hoặc quyết định đang xử lý -> ném lỗi 409")
    void applyResignation_AlreadyInProgress_ThrowsException() {
        ApplyResignationRequest req = ApplyResignationRequest.builder()
                .expectedLastWorkingDate(LocalDate.now().plusDays(40))
                .personalReasonCategory(ResignationReason.BETTER_OPPORTUNITY)
                .build();

        when(employeeService.findEmployeeIdByUserId(userId)).thenReturn(Optional.of(employeeId));
        when(employeeService.getEmployeeByIdInternal(employeeId)).thenReturn(mockEmployee);
        when(resignationRepository.existsByEmployeeIdAndStatusIn(eq(employeeId), any())).thenReturn(true);

        assertThatThrownBy(() -> service.applyResignation(req, userId))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getErrorCode()).isEqualTo(OffboardingErrorCode.OFFBOARDING_ALREADY_IN_PROGRESS));
    }

    @Test
    @DisplayName("Apply Resignation: chặn nộp đơn nếu chưa liên kết tài khoản nhân viên")
    void applyResignation_NotLinkedEmployee_ThrowsException() {
        ApplyResignationRequest req = ApplyResignationRequest.builder()
                .expectedLastWorkingDate(LocalDate.now().plusDays(30))
                .personalReasonCategory(ResignationReason.PERSONAL_FAMILY)
                .build();

        when(employeeService.findEmployeeIdByUserId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.applyResignation(req, userId))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getErrorCode()).isEqualTo(OffboardingErrorCode.CURRENT_USER_NOT_LINKED_EMPLOYEE));
    }

    @Test
    @DisplayName("Approve Resignation: phê duyệt đơn thôi việc thành công và tự động khởi tạo clearances")
    void approveResignation_Success() {
        UUID resId = UUID.randomUUID();
        Resignation r = Resignation.builder()
                .employeeId(employeeId)
                .companyId(companyId)
                .status(ResignationStatus.PENDING)
                .expectedLastWorkingDate(LocalDate.now().plusDays(30))
                .build();
        r.setId(resId);

        UUID approverId = UUID.randomUUID();
        when(resignationRepository.findById(resId)).thenReturn(Optional.of(r));
        when(employeeService.findEmployeeIdByUserId(userId)).thenReturn(Optional.of(approverId));
        when(resignationRepository.save(any(Resignation.class))).thenAnswer(inv -> inv.getArgument(0));
        when(employeeService.getEmployeeByIdInternal(employeeId)).thenReturn(mockEmployee);

        ApproveResignationRequest req = ApproveResignationRequest.builder()
                .isApproved(true)
                .effectiveLastWorkingDate(LocalDate.now().plusDays(30))
                .build();

        ResignationResponse res = service.approveResignation(resId, req, userId);

        assertThat(res).isNotNull();
        assertThat(r.getStatus()).isEqualTo(ResignationStatus.APPROVED);
        assertThat(r.getApprovedByEmployeeId()).isEqualTo(approverId);
        // Kiểm tra đã khởi tạo clearances khi duyệt
        verify(clearanceRepository, atLeastOnce()).save(any(OffboardingClearance.class));
    }

    @Test
    @DisplayName("Approve Resignation: từ chối đơn mà không nhập lý do -> ném lỗi 400")
    void approveResignation_RejectWithoutReason_ThrowsException() {
        UUID resId = UUID.randomUUID();
        Resignation r = Resignation.builder()
                .employeeId(employeeId)
                .companyId(companyId)
                .status(ResignationStatus.PENDING)
                .build();
        r.setId(resId);

        UUID approverId = UUID.randomUUID();
        when(resignationRepository.findById(resId)).thenReturn(Optional.of(r));
        when(employeeService.findEmployeeIdByUserId(userId)).thenReturn(Optional.of(approverId));

        ApproveResignationRequest req = ApproveResignationRequest.builder()
                .isApproved(false)
                .rejectionReason(null)
                .build();

        assertThatThrownBy(() -> service.approveResignation(resId, req, userId))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getErrorCode()).isEqualTo(OffboardingErrorCode.REJECTION_REASON_REQUIRED));
    }

    @Test
    @DisplayName("Update Clearance: chặn xác nhận hoàn trả tài sản nếu nhân viên vẫn còn giữ tài sản")
    void updateClearance_AssetUnreturned_ThrowsException() {
        UpdateClearanceRequest req = UpdateClearanceRequest.builder()
                .employeeId(employeeId)
                .clearanceType(ClearanceType.ASSET)
                .status(ClearanceStatus.CLEARED)
                .note("Bàn giao đầy đủ")
                .build();

        when(employeeService.getEmployeeByIdInternal(employeeId)).thenReturn(mockEmployee);

        OffboardingClearance c = OffboardingClearance.builder()
                .employeeId(employeeId)
                .clearanceType(ClearanceType.ASSET)
                .status(ClearanceStatus.PENDING)
                .build();
        when(clearanceRepository.findByEmployeeIdAndClearanceType(employeeId, ClearanceType.ASSET))
                .thenReturn(Optional.of(c));

        // Còn 2 thiết bị chưa trả về kho
        when(assetService.countUnreturnedAssets(employeeId)).thenReturn(2L);

        assertThatThrownBy(() -> service.updateClearance(req, userId))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getErrorCode()).isEqualTo(OffboardingErrorCode.UNRETURNED_ASSETS_EXIST));
    }

    @Test
    @DisplayName("Complete Offboarding: không có đơn Resignation/Termination APPROVED nào -> ném lỗi 422")
    void completeOffboarding_NoApprovedOffboarding_ThrowsException() {
        when(employeeService.getEmployeeByIdInternal(employeeId)).thenReturn(mockEmployee);
        when(resignationRepository.findFirstByEmployeeIdAndStatus(employeeId, ResignationStatus.APPROVED))
                .thenReturn(Optional.empty());
        when(terminationRepository.findFirstByEmployeeIdAndStatus(employeeId, TerminationStatus.APPROVED))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.completeOffboarding(employeeId, userId))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getErrorCode()).isEqualTo(OffboardingErrorCode.NO_APPROVED_OFFBOARDING));
    }

    @Test
    @DisplayName("Complete Offboarding: danh sách clearance rỗng -> ném lỗi CLEARANCES_INCOMPLETE")
    void completeOffboarding_EmptyClearances_ThrowsException() {
        when(employeeService.getEmployeeByIdInternal(employeeId)).thenReturn(mockEmployee);
        when(resignationRepository.findFirstByEmployeeIdAndStatus(employeeId, ResignationStatus.APPROVED))
                .thenReturn(Optional.of(new Resignation()));
        when(assetService.countUnreturnedAssets(employeeId)).thenReturn(0L);
        when(clearanceRepository.findByEmployeeId(employeeId)).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> service.completeOffboarding(employeeId, userId))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getErrorCode()).isEqualTo(OffboardingErrorCode.CLEARANCES_INCOMPLETE));
    }

    @Test
    @DisplayName("Complete Offboarding: chặn hoàn tất nếu thủ tục bàn giao chưa đủ (CLEARANCES_INCOMPLETE)")
    void completeOffboarding_ClearanceIncomplete_ThrowsException() {
        when(employeeService.getEmployeeByIdInternal(employeeId)).thenReturn(mockEmployee);
        when(resignationRepository.findFirstByEmployeeIdAndStatus(employeeId, ResignationStatus.APPROVED))
                .thenReturn(Optional.of(new Resignation()));

        // Hết tài sản
        when(assetService.countUnreturnedAssets(employeeId)).thenReturn(0L);

        // Vẫn còn mục PENDING
        OffboardingClearance it = OffboardingClearance.builder().clearanceType(ClearanceType.IT).status(ClearanceStatus.PENDING).build();
        when(clearanceRepository.findByEmployeeId(employeeId)).thenReturn(List.of(it));

        assertThatThrownBy(() -> service.completeOffboarding(employeeId, userId))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getErrorCode()).isEqualTo(OffboardingErrorCode.CLEARANCES_INCOMPLETE));
    }

    @Test
    @DisplayName("Complete Offboarding: thành công với đơn thôi việc, đổi trạng thái sang RESIGNED và vô hiệu hóa tài khoản User")
    void completeOffboarding_Resignation_Success_DeactivatesUser() {
        UUID systemUserId = UUID.randomUUID();
        mockEmployee.setUserId(systemUserId);
        when(permEvaluator.has("offboarding.view")).thenReturn(true);
        when(employeeService.getEmployeeByIdInternal(employeeId)).thenReturn(mockEmployee);

        // Có đơn Resignation APPROVED
        when(resignationRepository.findFirstByEmployeeIdAndStatus(employeeId, ResignationStatus.APPROVED))
                .thenReturn(Optional.of(new Resignation()));
        when(terminationRepository.findFirstByEmployeeIdAndStatus(employeeId, TerminationStatus.APPROVED))
                .thenReturn(Optional.empty());

        // Tài sản = 0
        when(assetService.countUnreturnedAssets(employeeId)).thenReturn(0L);

        // Tất cả clearance đã CLEARED
        OffboardingClearance c1 = OffboardingClearance.builder().clearanceType(ClearanceType.IT).status(ClearanceStatus.CLEARED).build();
        OffboardingClearance c2 = OffboardingClearance.builder().clearanceType(ClearanceType.ASSET).status(ClearanceStatus.CLEARED).build();
        when(clearanceRepository.findByEmployeeId(employeeId)).thenReturn(List.of(c1, c2));

        when(leaveBalanceService.getTotalRemainingLeaveDays(eq(employeeId), anyInt())).thenReturn(BigDecimal.ZERO);
        when(exitInterviewRepository.findByEmployeeId(employeeId)).thenReturn(Optional.empty());

        OffboardingSummaryResponse summary = service.completeOffboarding(employeeId, userId);

        assertThat(summary).isNotNull();
        verify(employeeService).updateEmploymentStatus(employeeId, EmploymentStatus.RESIGNED);
        verify(userService).updateUserStatus(systemUserId, UserStatus.DISABLED);
    }

    @Test
    @DisplayName("Complete Offboarding: thành công với quyết định chấm dứt HĐ/sa thải, đổi trạng thái sang TERMINATED")
    void completeOffboarding_Termination_Success_UpdatesToTerminated() {
        UUID systemUserId = UUID.randomUUID();
        mockEmployee.setUserId(systemUserId);
        when(permEvaluator.has("offboarding.view")).thenReturn(true);
        when(employeeService.getEmployeeByIdInternal(employeeId)).thenReturn(mockEmployee);

        // Có Termination APPROVED
        when(terminationRepository.findFirstByEmployeeIdAndStatus(employeeId, TerminationStatus.APPROVED))
                .thenReturn(Optional.of(new Termination()));

        // Tài sản = 0
        when(assetService.countUnreturnedAssets(employeeId)).thenReturn(0L);

        OffboardingClearance c1 = OffboardingClearance.builder().clearanceType(ClearanceType.HR).status(ClearanceStatus.CLEARED).build();
        when(clearanceRepository.findByEmployeeId(employeeId)).thenReturn(List.of(c1));

        when(leaveBalanceService.getTotalRemainingLeaveDays(eq(employeeId), anyInt())).thenReturn(BigDecimal.ZERO);
        when(exitInterviewRepository.findByEmployeeId(employeeId)).thenReturn(Optional.empty());

        OffboardingSummaryResponse summary = service.completeOffboarding(employeeId, userId);

        assertThat(summary).isNotNull();
        verify(employeeService).updateEmploymentStatus(employeeId, EmploymentStatus.TERMINATED);
        verify(userService).updateUserStatus(systemUserId, UserStatus.DISABLED);
    }
}
