package com.cyclosa.attendance;

import com.cyclosa.attendance.dto.request.CreateExplanationRequest;
import com.cyclosa.attendance.dto.response.AttendanceExplanationResponse;
import com.cyclosa.attendance.entity.AttendanceExplanation;
import com.cyclosa.attendance.entity.AttendanceRecord;
import com.cyclosa.attendance.entity.Shift;
import com.cyclosa.attendance.enums.AttendanceStatus;
import com.cyclosa.attendance.enums.ExplanationReasonType;
import com.cyclosa.attendance.enums.ExplanationStatus;
import com.cyclosa.attendance.mapper.AttendanceExplanationMapper;
import com.cyclosa.attendance.repository.AttendanceExplanationRepository;
import com.cyclosa.attendance.repository.AttendanceRecordRepository;
import com.cyclosa.attendance.service.AttendanceExplanationService;
import com.cyclosa.attendance.service.ShiftAssignmentService;
import com.cyclosa.attendance.service.ShiftService;
import com.cyclosa.attendance.service.TimesheetService;
import com.cyclosa.common.dto.summary.CompanySummary;
import com.cyclosa.common.dto.summary.EmployeeSummary;
import com.cyclosa.common.exception.AppException;
import com.cyclosa.common.security.SecurityPermissionEvaluator;
import com.cyclosa.employee.dto.response.EmployeeDetailResponse;
import com.cyclosa.employee.service.EmployeeService;
import com.cyclosa.workflow.dto.response.WorkflowInstanceResponse;
import com.cyclosa.workflow.enums.ApprovalStatus;
import com.cyclosa.workflow.service.WorkflowEngineService;
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
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttendanceExplanationServiceTest {

    @Mock
    private AttendanceExplanationRepository explanationRepository;

    @Mock
    private AttendanceRecordRepository recordRepository;

    @Mock
    private ShiftService shiftService;

    @Mock
    private ShiftAssignmentService assignmentService;

    @Mock
    private TimesheetService timesheetService;

    @Mock
    private EmployeeService employeeService;

    @Mock
    private WorkflowEngineService workflowEngineService;

    @Mock
    private SecurityPermissionEvaluator permEvaluator;

    @Spy
    private AttendanceExplanationMapper explanationMapper = Mappers.getMapper(AttendanceExplanationMapper.class);

    @InjectMocks
    private AttendanceExplanationService explanationService;

    private UUID userId;
    private UUID employeeId;
    private UUID companyId;
    private EmployeeDetailResponse empDetail;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        employeeId = UUID.randomUUID();
        companyId = UUID.randomUUID();

        empDetail = EmployeeDetailResponse.builder()
                .id(employeeId)
                .company(CompanySummary.builder().id(companyId).name("CYCLOSA").build())
                .build();
    }

    @Test
    @DisplayName("Tạo đơn giải trình thành công và kích hoạt Workflow Engine")
    void testCreateExplanationSuccess() {
        LocalDate date = LocalDate.of(2026, 10, 5);
        CreateExplanationRequest req = CreateExplanationRequest.builder()
                .workDate(date)
                .reasonType(ExplanationReasonType.FORGOT_CHECK_IN)
                .proposedCheckIn(LocalTime.of(8, 0))
                .proposedCheckOut(LocalTime.of(17, 30))
                .reason("Quên dập thẻ do họp khẩn")
                .build();

        when(employeeService.findEmployeeIdByUserId(userId)).thenReturn(Optional.of(employeeId));
        when(employeeService.getEmployeeByIdInternal(employeeId)).thenReturn(empDetail);
        when(explanationRepository.existsByEmployeeIdAndWorkDateAndStatus(employeeId, date, ExplanationStatus.PENDING))
                .thenReturn(false);

        AttendanceExplanation saved = AttendanceExplanation.builder()
                .companyId(companyId)
                .employeeId(employeeId)
                .workDate(date)
                .reasonType(req.getReasonType())
                .reason(req.getReason())
                .status(ExplanationStatus.PENDING)
                .build();
        saved.setId(UUID.randomUUID());

        when(explanationRepository.save(any(AttendanceExplanation.class))).thenReturn(saved);
        when(workflowEngineService.startWorkflow(any())).thenReturn(
                WorkflowInstanceResponse.builder().id(UUID.randomUUID()).build()
        );
        when(employeeService.getEmployeeSummary(employeeId)).thenReturn(
                EmployeeSummary.builder().id(employeeId).fullName("Nguyễn Văn A").build()
        );

        AttendanceExplanationResponse res = explanationService.createExplanation(userId, req);

        assertNotNull(res);
        assertEquals(ExplanationStatus.PENDING, res.getStatus());
        verify(workflowEngineService).startWorkflow(any());
    }

    @Test
    @DisplayName("Ném lỗi khi đã có đơn giải trình PENDING cho ngày này")
    void testCreateExplanationDuplicatePending() {
        LocalDate date = LocalDate.of(2026, 10, 5);
        CreateExplanationRequest req = CreateExplanationRequest.builder()
                .workDate(date)
                .reasonType(ExplanationReasonType.FORGOT_CHECK_IN)
                .reason("Quên dập thẻ")
                .build();

        when(employeeService.findEmployeeIdByUserId(userId)).thenReturn(Optional.of(employeeId));
        when(employeeService.getEmployeeByIdInternal(employeeId)).thenReturn(empDetail);
        when(explanationRepository.existsByEmployeeIdAndWorkDateAndStatus(employeeId, date, ExplanationStatus.PENDING))
                .thenReturn(true);

        assertThrows(AppException.class, () -> explanationService.createExplanation(userId, req));
        verify(workflowEngineService, never()).startWorkflow(any());
    }

    @Test
    @DisplayName("Khi đơn giải trình được APPROVED qua Workflow, cập nhật công sang EXPLAINED")
    void testHandleWorkflowApproved() {
        UUID explanationId = UUID.randomUUID();
        LocalDate date = LocalDate.of(2026, 10, 5);

        AttendanceExplanation exp = AttendanceExplanation.builder()
                .companyId(companyId)
                .employeeId(employeeId)
                .workDate(date)
                .proposedCheckIn(LocalTime.of(8, 0))
                .proposedCheckOut(LocalTime.of(17, 30))
                .status(ExplanationStatus.PENDING)
                .build();
        exp.setId(explanationId);

        when(explanationRepository.findById(explanationId)).thenReturn(Optional.of(exp));
        when(recordRepository.findByCompanyIdAndEmployeeIdAndWorkDate(companyId, employeeId, date))
                .thenReturn(Optional.empty());

        Shift defaultShift = Shift.builder()
                .companyId(companyId)
                .code("CA_HC")
                .name("Ca Hành Chính")
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(17, 30))
                .workingHours(BigDecimal.valueOf(8.00))
                .workUnits(BigDecimal.valueOf(1.00))
                .graceLateMinutes(15)
                .graceEarlyMinutes(0)
                .build();

        when(assignmentService.findAssignment(companyId, employeeId, date)).thenReturn(Optional.empty());
        when(shiftService.getDefaultShift(companyId)).thenReturn(defaultShift);

        explanationService.handleWorkflowCompleted(explanationId, ApprovalStatus.APPROVED);

        assertEquals(ExplanationStatus.APPROVED, exp.getStatus());
        verify(recordRepository).save(argThat(r -> r.getStatus() == AttendanceStatus.EXPLAINED));
        verify(timesheetService).recalculateTimesheetForEmployee(companyId, employeeId, date.getMonthValue(), date.getYear());
    }
}
