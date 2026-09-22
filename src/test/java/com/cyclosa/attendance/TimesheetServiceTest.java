package com.cyclosa.attendance;

import com.cyclosa.attendance.dto.request.LockTimesheetRequest;
import com.cyclosa.attendance.dto.request.RecalculateTimesheetRequest;
import com.cyclosa.attendance.dto.response.MonthlyTimesheetResponse;
import com.cyclosa.attendance.entity.AttendanceRecord;
import com.cyclosa.attendance.entity.MonthlyTimesheet;
import com.cyclosa.attendance.enums.AttendanceStatus;
import com.cyclosa.attendance.mapper.TimesheetMapper;
import com.cyclosa.attendance.repository.AttendanceRecordRepository;
import com.cyclosa.attendance.repository.MonthlyTimesheetRepository;
import com.cyclosa.attendance.service.TimesheetService;
import com.cyclosa.common.dto.summary.EmployeeAttendanceSummary;
import com.cyclosa.common.dto.summary.EmployeeSummary;
import com.cyclosa.common.security.SecurityPermissionEvaluator;
import com.cyclosa.common.util.SecurityUtils;
import com.cyclosa.employee.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TimesheetServiceTest {

    @Mock
    private MonthlyTimesheetRepository timesheetRepository;

    @Mock
    private AttendanceRecordRepository recordRepository;

    @Mock
    private EmployeeService employeeService;

    @Mock
    private SecurityPermissionEvaluator permEvaluator;

    @Spy
    private TimesheetMapper timesheetMapper = Mappers.getMapper(TimesheetMapper.class);

    @InjectMocks
    private TimesheetService timesheetService;

    private UUID companyId;
    private UUID employeeId;

    @BeforeEach
    void setUp() {
        companyId = UUID.randomUUID();
        employeeId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Xem bảng công cá nhân getMyTimesheet")
    void testGetMyTimesheet() {
        UUID currentUserId = UUID.randomUUID();

        try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
            securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(currentUserId);
            when(employeeService.findEmployeeIdByUserId(currentUserId)).thenReturn(Optional.of(employeeId));

            MonthlyTimesheet existing = MonthlyTimesheet.builder()
                    .companyId(companyId)
                    .employeeId(employeeId)
                    .month(10)
                    .year(2026)
                    .standardWorkDays(BigDecimal.valueOf(22.0))
                    .actualWorkDays(BigDecimal.valueOf(20.0))
                    .totalPaidDays(BigDecimal.valueOf(20.0))
                    .build();
            existing.setId(UUID.randomUUID());

            when(timesheetRepository.findByEmployeeIdAndMonthAndYear(employeeId, 10, 2026))
                    .thenReturn(Optional.of(existing));
            when(employeeService.getEmployeeSummary(employeeId))
                    .thenReturn(EmployeeSummary.builder().id(employeeId).fullName("Nguyễn Văn A").build());

            MonthlyTimesheetResponse res = timesheetService.getMyTimesheet(10, 2026);

            assertNotNull(res);
            assertEquals(BigDecimal.valueOf(20.0), res.getActualWorkDays());
            assertEquals("Nguyễn Văn A", res.getEmployee().getFullName());
        }
    }

    @Test
    @DisplayName("Tính toán lại bảng công tháng recalculateTimesheets")
    void testRecalculateTimesheets() {
        RecalculateTimesheetRequest req = RecalculateTimesheetRequest.builder()
                .month(10)
                .year(2026)
                .employeeIds(Set.of(employeeId))
                .build();

        AttendanceRecord rec = AttendanceRecord.builder()
                .companyId(companyId)
                .employeeId(employeeId)
                .workDate(LocalDate.of(2026, 10, 1))
                .actualWorkUnits(BigDecimal.valueOf(1.0))
                .lateMinutes(0)
                .earlyMinutes(0)
                .status(AttendanceStatus.ON_TIME)
                .build();

        when(timesheetRepository.findByCompanyIdAndEmployeeIdAndMonthAndYear(companyId, employeeId, 10, 2026))
                .thenReturn(Optional.empty());
        when(recordRepository.findAllByEmployeeIdAndWorkDateBetweenOrderByWorkDateAsc(eq(employeeId), any(), any()))
                .thenReturn(List.of(rec));
        when(timesheetRepository.save(any(MonthlyTimesheet.class))).thenAnswer(i -> {
            MonthlyTimesheet ts = i.getArgument(0);
            ts.setId(UUID.randomUUID());
            return ts;
        });
        when(employeeService.getEmployeeSummaries(Set.of(employeeId))).thenReturn(Map.of(
                employeeId, EmployeeSummary.builder().id(employeeId).fullName("Nguyễn Văn A").build()
        ));

        List<MonthlyTimesheetResponse> result = timesheetService.recalculateTimesheets(companyId, req);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(BigDecimal.valueOf(1.0).setScale(2), result.get(0).getActualWorkDays());
        verify(timesheetRepository).save(any(MonthlyTimesheet.class));
    }

    @Test
    @DisplayName("Chốt khóa bảng công tháng lockTimesheet")
    void testLockTimesheet() {
        LockTimesheetRequest req = LockTimesheetRequest.builder()
                .month(10)
                .year(2026)
                .build();

        MonthlyTimesheet ts = MonthlyTimesheet.builder()
                .companyId(companyId)
                .employeeId(employeeId)
                .month(10)
                .year(2026)
                .isLocked(false)
                .build();

        when(recordRepository.findAllByCompanyIdAndWorkDateBetween(eq(companyId), any(), any()))
                .thenReturn(List.of());
        when(timesheetRepository.findAllByCompanyIdAndMonthAndYear(companyId, 10, 2026))
                .thenReturn(List.of(ts));

        timesheetService.lockTimesheet(companyId, req);

        assertTrue(ts.getIsLocked());
        verify(timesheetRepository).saveAll(any());
    }

    @Test
    @DisplayName("Cung cấp dữ liệu bảng công cho Module 08 Payroll getMonthlyAttendanceSummaries")
    void testGetMonthlyAttendanceSummaries() {
        MonthlyTimesheet ts = MonthlyTimesheet.builder()
                .companyId(companyId)
                .employeeId(employeeId)
                .month(10)
                .year(2026)
                .standardWorkDays(BigDecimal.valueOf(22.0))
                .actualWorkDays(BigDecimal.valueOf(21.5))
                .paidLeaveDays(BigDecimal.valueOf(0.5))
                .totalPaidDays(BigDecimal.valueOf(22.0))
                .totalLateMinutes(10)
                .totalEarlyMinutes(0)
                .missingPunchCount(0)
                .build();

        when(timesheetRepository.findAllByCompanyIdAndEmployeeIdInAndMonthAndYear(companyId, Set.of(employeeId), 10, 2026))
                .thenReturn(List.of(ts));

        Map<UUID, EmployeeAttendanceSummary> map = timesheetService.getMonthlyAttendanceSummaries(
                companyId, 10, 2026, Set.of(employeeId)
        );

        assertNotNull(map);
        assertTrue(map.containsKey(employeeId));
        EmployeeAttendanceSummary summary = map.get(employeeId);
        assertEquals(BigDecimal.valueOf(21.5), summary.getActualWorkDays());
        assertEquals(10, summary.getTotalLateMinutes());
    }
}
