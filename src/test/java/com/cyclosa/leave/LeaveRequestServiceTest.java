package com.cyclosa.leave;

import com.cyclosa.attendance.entity.AttendanceRecord;
import com.cyclosa.attendance.repository.AttendanceRecordRepository;
import com.cyclosa.common.dto.summary.CompanySummary;
import com.cyclosa.common.dto.summary.EmployeeSummary;
import com.cyclosa.common.exception.AppException;
import com.cyclosa.common.security.SecurityPermissionEvaluator;
import com.cyclosa.employee.dto.response.EmployeeDetailResponse;
import com.cyclosa.employee.service.EmployeeService;
import com.cyclosa.leave.dto.request.CreateLeaveRequestRequest;
import com.cyclosa.leave.dto.response.LeaveRequestResponse;
import com.cyclosa.leave.dto.response.LeaveTypeResponse;
import com.cyclosa.leave.entity.LeaveBalance;
import com.cyclosa.leave.entity.LeaveRequest;
import com.cyclosa.leave.entity.LeaveType;
import com.cyclosa.leave.enums.FundingSource;
import com.cyclosa.leave.enums.LeaveCategory;
import com.cyclosa.leave.enums.LeaveRequestStatus;
import com.cyclosa.leave.enums.LeaveSession;
import com.cyclosa.leave.exception.LeaveErrorCode;
import com.cyclosa.leave.mapper.LeaveRequestMapper;
import com.cyclosa.leave.repository.LeaveBalanceRepository;
import com.cyclosa.leave.repository.LeaveRequestRepository;
import com.cyclosa.leave.repository.LeaveTypeRepository;
import com.cyclosa.leave.service.LeaveBalanceService;
import com.cyclosa.leave.service.LeaveCalculationEngine;
import com.cyclosa.leave.service.LeaveRequestService;
import com.cyclosa.leave.service.LeaveTypeService;
import com.cyclosa.organization.service.CompanyService;
import com.cyclosa.workflow.dto.request.StartWorkflowRequest;
import com.cyclosa.workflow.dto.response.WorkflowInstanceResponse;
import com.cyclosa.workflow.service.WorkflowEngineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeaveRequestServiceTest {

    @Mock
    private LeaveRequestRepository requestRepository;
    @Mock
    private LeaveTypeRepository leaveTypeRepository;
    @Mock
    private LeaveBalanceRepository balanceRepository;
    @Mock
    private AttendanceRecordRepository attendanceRecordRepository;
    @Mock
    private LeaveBalanceService balanceService;
    @Mock
    private LeaveTypeService leaveTypeService;
    @Mock
    private LeaveCalculationEngine calculationEngine;
    @Mock
    private EmployeeService employeeService;
    @Mock
    private CompanyService companyService;
    @Mock
    private WorkflowEngineService workflowEngineService;
    @Mock
    private SecurityPermissionEvaluator permEvaluator;

    private LeaveRequestMapper requestMapper = Mappers.getMapper(LeaveRequestMapper.class);

    private LeaveRequestService service;

    private UUID currentUserId;
    private UUID companyId;
    private UUID employeeId;
    private UUID leaveTypeId;

    @BeforeEach
    void setUp() {
        service = new LeaveRequestService(
                requestRepository, leaveTypeRepository, balanceRepository, attendanceRecordRepository,
                balanceService, leaveTypeService, calculationEngine, employeeService, companyService,
                workflowEngineService, permEvaluator, requestMapper
        );

        currentUserId = UUID.randomUUID();
        companyId = UUID.randomUUID();
        employeeId = UUID.randomUUID();
        leaveTypeId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Tạo đơn nghỉ phép thành công khi cần duyệt qua Workflow")
    void testCreateLeaveRequest_withWorkflow_success() {
        LocalDate startDate = LocalDate.now().plusDays(3);
        LocalDate endDate = LocalDate.now().plusDays(4);

        CreateLeaveRequestRequest req = CreateLeaveRequestRequest.builder()
                .leaveTypeId(leaveTypeId)
                .startDate(startDate)
                .endDate(endDate)
                .session(LeaveSession.FULL_DAY)
                .reason("Nghỉ phép cá nhân")
                .build();

        EmployeeDetailResponse empDetail = EmployeeDetailResponse.builder()
                .id(employeeId)
                .company(CompanySummary.builder().id(companyId).build())
                .build();

        when(employeeService.findEmployeeIdByUserId(currentUserId)).thenReturn(Optional.of(employeeId));
        when(employeeService.getEmployeeByIdInternal(employeeId)).thenReturn(empDetail);
        when(permEvaluator.has("leave.manage")).thenReturn(false);

        LeaveType leaveType = LeaveType.builder()
                .code("ANNUAL_LEAVE")
                .category(LeaveCategory.ANNUAL)
                .fundingSource(FundingSource.COMPANY)
                .isPaid(true)
                .requiresApproval(true)
                .isActive(true)
                .build();
        leaveType.setId(leaveTypeId);
        when(leaveTypeRepository.findById(leaveTypeId)).thenReturn(Optional.of(leaveType));

        BigDecimal requestedDays = BigDecimal.valueOf(2.0);
        when(calculationEngine.calculateLeaveDays(leaveType, startDate, endDate, LeaveSession.FULL_DAY))
                .thenReturn(requestedDays);

        when(requestRepository.hasOverlappingRequest(eq(employeeId), eq(startDate), eq(endDate), any(), isNull()))
                .thenReturn(false);

        when(attendanceRecordRepository.findAllByEmployeeIdAndWorkDateBetweenOrderByWorkDateAsc(employeeId, startDate, endDate))
                .thenReturn(Collections.emptyList());

        LeaveBalance balance = LeaveBalance.builder()
                .companyId(companyId)
                .employeeId(employeeId)
                .leaveTypeId(leaveTypeId)
                .year(startDate.getYear())
                .totalDays(BigDecimal.valueOf(12.0))
                .usedDays(BigDecimal.valueOf(2.0))
                .pendingDays(BigDecimal.ZERO)
                .build();
        when(balanceRepository.findByCompanyIdAndEmployeeIdAndLeaveTypeIdAndYear(companyId, employeeId, leaveTypeId, startDate.getYear()))
                .thenReturn(Optional.of(balance));

        when(requestRepository.save(any(LeaveRequest.class))).thenAnswer(inv -> {
            LeaveRequest r = inv.getArgument(0);
            if (r.getId() == null) {
                r.setId(UUID.randomUUID());
            }
            return r;
        });

        UUID wfId = UUID.randomUUID();
        when(workflowEngineService.startWorkflow(any(StartWorkflowRequest.class)))
                .thenReturn(WorkflowInstanceResponse.builder().id(wfId).build());

        when(employeeService.getEmployeeSummary(employeeId))
                .thenReturn(EmployeeSummary.builder().id(employeeId).fullName("Nguyễn Văn A").build());
        when(leaveTypeService.getLeaveTypeById(companyId, leaveTypeId))
                .thenReturn(LeaveTypeResponse.builder().id(leaveTypeId).name("Nghỉ phép năm").build());

        LeaveRequestResponse response = service.createLeaveRequest(currentUserId, req);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(LeaveRequestStatus.PENDING_APPROVAL);
        assertThat(response.getTotalDays()).isEqualByComparingTo(requestedDays);

        verify(balanceService).recordPendingLeave(companyId, employeeId, leaveTypeId, startDate.getYear(), requestedDays);
        verify(workflowEngineService).startWorkflow(any(StartWorkflowRequest.class));
    }

    @Test
    @DisplayName("Ném ngoại lệ khi số dư phép khả dụng không đủ")
    void testCreateLeaveRequest_insufficientBalance_throwsException() {
        LocalDate startDate = LocalDate.now().plusDays(3);
        LocalDate endDate = LocalDate.now().plusDays(7);

        CreateLeaveRequestRequest req = CreateLeaveRequestRequest.builder()
                .leaveTypeId(leaveTypeId)
                .startDate(startDate)
                .endDate(endDate)
                .session(LeaveSession.FULL_DAY)
                .reason("Nghỉ phép dài ngày")
                .build();

        EmployeeDetailResponse empDetail = EmployeeDetailResponse.builder()
                .id(employeeId)
                .company(CompanySummary.builder().id(companyId).build())
                .build();

        when(employeeService.findEmployeeIdByUserId(currentUserId)).thenReturn(Optional.of(employeeId));
        when(employeeService.getEmployeeByIdInternal(employeeId)).thenReturn(empDetail);
        when(permEvaluator.has("leave.manage")).thenReturn(false);

        LeaveType leaveType = LeaveType.builder()
                .category(LeaveCategory.ANNUAL)
                .isActive(true)
                .build();
        leaveType.setId(leaveTypeId);
        when(leaveTypeRepository.findById(leaveTypeId)).thenReturn(Optional.of(leaveType));

        when(calculationEngine.calculateLeaveDays(leaveType, startDate, endDate, LeaveSession.FULL_DAY))
                .thenReturn(BigDecimal.valueOf(5.0));
        when(requestRepository.hasOverlappingRequest(eq(employeeId), eq(startDate), eq(endDate), any(), isNull()))
                .thenReturn(false);

        LeaveBalance lowBalance = LeaveBalance.builder()
                .companyId(companyId)
                .employeeId(employeeId)
                .leaveTypeId(leaveTypeId)
                .year(startDate.getYear())
                .totalDays(BigDecimal.valueOf(12.0))
                .usedDays(BigDecimal.valueOf(10.0)) // còn 2 ngày khả dụng, cần 5
                .pendingDays(BigDecimal.ZERO)
                .build();
        when(balanceRepository.findByCompanyIdAndEmployeeIdAndLeaveTypeIdAndYear(companyId, employeeId, leaveTypeId, startDate.getYear()))
                .thenReturn(Optional.of(lowBalance));

        assertThatThrownBy(() -> service.createLeaveRequest(currentUserId, req))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Số dư phép khả dụng không đủ");
    }

    @Test
    @DisplayName("Ném ngoại lệ khi ngày xin nghỉ đã có chấm công đi làm")
    void testCreateLeaveRequest_alreadyWorked_throwsException() {
        LocalDate startDate = LocalDate.now().plusDays(2);
        LocalDate endDate = LocalDate.now().plusDays(2);

        CreateLeaveRequestRequest req = CreateLeaveRequestRequest.builder()
                .leaveTypeId(leaveTypeId)
                .startDate(startDate)
                .endDate(endDate)
                .session(LeaveSession.FULL_DAY)
                .reason("Nghỉ")
                .build();

        EmployeeDetailResponse empDetail = EmployeeDetailResponse.builder()
                .id(employeeId)
                .company(CompanySummary.builder().id(companyId).build())
                .build();

        when(employeeService.findEmployeeIdByUserId(currentUserId)).thenReturn(Optional.of(employeeId));
        when(employeeService.getEmployeeByIdInternal(employeeId)).thenReturn(empDetail);

        LeaveType leaveType = LeaveType.builder().category(LeaveCategory.ANNUAL).isActive(true).build();
        leaveType.setId(leaveTypeId);
        when(leaveTypeRepository.findById(leaveTypeId)).thenReturn(Optional.of(leaveType));
        when(calculationEngine.calculateLeaveDays(leaveType, startDate, endDate, LeaveSession.FULL_DAY)).thenReturn(BigDecimal.valueOf(1.0));

        // Giả lập đã có check-in vào ngày startDate
        AttendanceRecord record = AttendanceRecord.builder()
                .workDate(startDate)
                .checkInTime(LocalDateTime.now())
                .build();
        when(attendanceRecordRepository.findAllByEmployeeIdAndWorkDateBetweenOrderByWorkDateAsc(employeeId, startDate, endDate))
                .thenReturn(List.of(record));

        assertThatThrownBy(() -> service.createLeaveRequest(currentUserId, req))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("đã thực hiện chấm công đi làm");
    }

    @Test
    @DisplayName("Hủy đơn nghỉ phép thành công khi ngày nghỉ chưa bắt đầu")
    void testCancelLeaveRequest_success() {
        UUID reqId = UUID.randomUUID();
        LocalDate futureDate = LocalDate.now().plusDays(5);

        LeaveRequest req = LeaveRequest.builder()
                .companyId(companyId)
                .employeeId(employeeId)
                .leaveTypeId(leaveTypeId)
                .startDate(futureDate)
                .endDate(futureDate)
                .status(LeaveRequestStatus.PENDING_APPROVAL)
                .totalDays(BigDecimal.valueOf(1.0))
                .build();
        req.setId(reqId);

        when(requestRepository.findById(reqId)).thenReturn(Optional.of(req));
        when(employeeService.findEmployeeIdByUserId(currentUserId)).thenReturn(Optional.of(employeeId));
        when(requestRepository.save(any(LeaveRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        LeaveRequestResponse res = service.cancelLeaveRequest(currentUserId, reqId);

        assertThat(res).isNotNull();
        assertThat(req.getStatus()).isEqualTo(LeaveRequestStatus.CANCELLED);
        verify(balanceService).rollbackLeave(req, false);
    }
}
