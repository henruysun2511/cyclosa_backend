package com.cyclosa.attendance;

import com.cyclosa.attendance.dto.request.CheckInRequest;
import com.cyclosa.attendance.dto.request.CheckOutRequest;
import com.cyclosa.attendance.dto.response.AttendanceRecordResponse;
import com.cyclosa.attendance.entity.AttendanceRecord;
import com.cyclosa.attendance.entity.Shift;
import com.cyclosa.attendance.entity.ShiftAssignment;
import com.cyclosa.attendance.enums.AttendanceStatus;
import com.cyclosa.attendance.enums.CheckMethod;
import com.cyclosa.attendance.enums.ShiftAssignmentStatus;
import com.cyclosa.attendance.exception.AttendanceErrorCode;
import com.cyclosa.attendance.mapper.AttendanceRecordMapper;
import com.cyclosa.attendance.mapper.ShiftMapper;
import com.cyclosa.attendance.repository.AttendanceRecordRepository;
import com.cyclosa.attendance.repository.ShiftAssignmentRepository;
import com.cyclosa.attendance.service.AttendanceRecordService;
import com.cyclosa.attendance.service.ShiftAssignmentService;
import com.cyclosa.attendance.service.ShiftService;
import com.cyclosa.common.dto.summary.CompanySummary;
import com.cyclosa.common.exception.AppException;
import com.cyclosa.common.security.SecurityPermissionEvaluator;
import com.cyclosa.employee.dto.response.EmployeeDetailResponse;
import com.cyclosa.employee.service.EmployeeService;
import com.cyclosa.organization.service.GeographyService;
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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttendanceRecordServiceTest {

    @Mock
    private AttendanceRecordRepository recordRepository;

    @Mock
    private ShiftAssignmentRepository shiftAssignmentRepository;

    @Mock
    private ShiftService shiftService;

    @Mock
    private ShiftAssignmentService assignmentService;

    @Mock
    private EmployeeService employeeService;

    @Mock
    private GeographyService geographyService;

    @Mock
    private SecurityPermissionEvaluator permEvaluator;

    @Spy
    private AttendanceRecordMapper recordMapper = Mappers.getMapper(AttendanceRecordMapper.class);

    @Spy
    private ShiftMapper shiftMapper = Mappers.getMapper(ShiftMapper.class);

    @InjectMocks
    private AttendanceRecordService recordService;

    private UUID userId;
    private UUID employeeId;
    private UUID companyId;
    private Shift shift;
    private EmployeeDetailResponse empDetail;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        employeeId = UUID.randomUUID();
        companyId = UUID.randomUUID();

        shift = Shift.builder()
                .companyId(companyId)
                .code("CA_HC")
                .name("Ca Hành Chính")
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(17, 30))
                .breakStartTime(LocalTime.of(12, 0))
                .breakEndTime(LocalTime.of(13, 30))
                .workingHours(BigDecimal.valueOf(8.00))
                .workUnits(BigDecimal.valueOf(1.00))
                .graceLateMinutes(15)
                .graceEarlyMinutes(0)
                .isNightShift(false)
                .build();
        shift.setId(UUID.randomUUID());

        empDetail = EmployeeDetailResponse.builder()
                .id(employeeId)
                .company(CompanySummary.builder().id(companyId).name("Công ty CYCLOSA").build())
                .build();
    }

    @Test
    @DisplayName("Thực hiện Check-in thành công")
    void testCheckInSuccess() {
        CheckInRequest req = CheckInRequest.builder()
                .latitude(10.7769)
                .longitude(106.7009)
                .method(CheckMethod.GPS)
                .build();

        when(employeeService.findEmployeeIdByUserId(userId)).thenReturn(Optional.of(employeeId));
        when(employeeService.getEmployeeByIdInternal(employeeId)).thenReturn(empDetail);
        when(recordRepository.findByCompanyIdAndEmployeeIdAndWorkDate(eq(companyId), eq(employeeId), any(LocalDate.class)))
                .thenReturn(Optional.empty());
        when(assignmentService.findAssignment(eq(companyId), eq(employeeId), any(LocalDate.class)))
                .thenReturn(Optional.empty());
        when(shiftService.getDefaultShift(companyId)).thenReturn(shift);
        when(recordRepository.save(any(AttendanceRecord.class))).thenAnswer(i -> {
            AttendanceRecord r = i.getArgument(0);
            r.setId(UUID.randomUUID());
            return r;
        });

        AttendanceRecordResponse res = recordService.checkIn(userId, req);

        assertNotNull(res);
        assertNotNull(res.getCheckInTime());
        verify(recordRepository).save(any(AttendanceRecord.class));
    }

    @Test
    @DisplayName("Ném lỗi khi Check-in lần thứ 2 trong cùng ngày")
    void testCheckInAlreadyCheckedIn() {
        CheckInRequest req = CheckInRequest.builder().build();

        AttendanceRecord existing = AttendanceRecord.builder()
                .companyId(companyId)
                .employeeId(employeeId)
                .workDate(LocalDate.now())
                .checkInTime(LocalDateTime.now())
                .build();

        when(employeeService.findEmployeeIdByUserId(userId)).thenReturn(Optional.of(employeeId));
        when(employeeService.getEmployeeByIdInternal(employeeId)).thenReturn(empDetail);
        when(recordRepository.findByCompanyIdAndEmployeeIdAndWorkDate(eq(companyId), eq(employeeId), any(LocalDate.class)))
                .thenReturn(Optional.of(existing));

        AppException ex = assertThrows(AppException.class, () -> recordService.checkIn(userId, req));
        assertEquals(AttendanceErrorCode.ATTENDANCE_ALREADY_CHECKED_IN.getCode(), ex.getErrorCode().getCode());
    }

    @Test
    @DisplayName("Thực hiện Check-out thành công và tính công chuẩn xác")
    void testCheckOutSuccess() {
        CheckOutRequest req = CheckOutRequest.builder()
                .method(CheckMethod.GPS)
                .build();

        AttendanceRecord existing = AttendanceRecord.builder()
                .companyId(companyId)
                .employeeId(employeeId)
                .workDate(LocalDate.now())
                .checkInTime(LocalDateTime.now().minusHours(9))
                .shiftId(shift.getId())
                .build();
        existing.setId(UUID.randomUUID());

        when(employeeService.findEmployeeIdByUserId(userId)).thenReturn(Optional.of(employeeId));
        when(employeeService.getEmployeeByIdInternal(employeeId)).thenReturn(empDetail);
        when(recordRepository.findByCompanyIdAndEmployeeIdAndWorkDate(eq(companyId), eq(employeeId), any(LocalDate.class)))
                .thenReturn(Optional.of(existing));
        when(shiftService.findShiftEntity(companyId, shift.getId())).thenReturn(shift);
        when(recordRepository.save(any(AttendanceRecord.class))).thenAnswer(i -> i.getArgument(0));

        AttendanceRecordResponse res = recordService.checkOut(userId, req);

        assertNotNull(res);
        assertNotNull(res.getCheckOutTime());
        assertNotNull(res.getActualHours());
        assertNotNull(res.getActualWorkUnits());
        verify(recordRepository).save(any(AttendanceRecord.class));
    }

    @Test
    @DisplayName("Check-out ca đêm fallback tìm bản ghi hôm trước nếu hôm nay chưa check-in")
    void testCheckOutNightShiftFallback() {
        CheckOutRequest req = CheckOutRequest.builder().build();

        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        AttendanceRecord yesterdayRecord = AttendanceRecord.builder()
                .companyId(companyId)
                .employeeId(employeeId)
                .workDate(yesterday)
                .checkInTime(yesterday.atTime(22, 0))
                .shiftId(shift.getId())
                .build();
        yesterdayRecord.setId(UUID.randomUUID());

        when(employeeService.findEmployeeIdByUserId(userId)).thenReturn(Optional.of(employeeId));
        when(employeeService.getEmployeeByIdInternal(employeeId)).thenReturn(empDetail);
        when(recordRepository.findByCompanyIdAndEmployeeIdAndWorkDate(companyId, employeeId, today))
                .thenReturn(Optional.empty());
        when(recordRepository.findByCompanyIdAndEmployeeIdAndWorkDateAndCheckInTimeIsNotNullAndCheckOutTimeIsNull(companyId, employeeId, yesterday))
                .thenReturn(Optional.of(yesterdayRecord));
        when(shiftService.findShiftEntity(companyId, shift.getId())).thenReturn(shift);
        when(recordRepository.save(any(AttendanceRecord.class))).thenAnswer(i -> i.getArgument(0));

        AttendanceRecordResponse res = recordService.checkOut(userId, req);

        assertNotNull(res);
        assertNotNull(res.getCheckOutTime());
        assertEquals(yesterday, res.getWorkDate());
    }

    @Test
    @DisplayName("Chốt công ngày sweepDailyAttendanceRecords đánh dấu MISSING_CHECK_OUT và tạo ABSENT")
    void testSweepDailyAttendanceRecords() {
        LocalDate date = LocalDate.of(2026, 10, 5);

        AttendanceRecord unclosed = AttendanceRecord.builder()
                .companyId(companyId)
                .employeeId(employeeId)
                .workDate(date)
                .checkInTime(date.atTime(8, 0))
                .status(AttendanceStatus.ON_TIME)
                .build();

        when(recordRepository.findAllByWorkDateAndCheckInTimeIsNotNullAndCheckOutTimeIsNull(date))
                .thenReturn(List.of(unclosed));

        UUID absentEmpId = UUID.randomUUID();
        ShiftAssignment sa = ShiftAssignment.builder()
                .companyId(companyId)
                .employeeId(absentEmpId)
                .shift(shift)
                .assignedDate(date)
                .status(ShiftAssignmentStatus.ASSIGNED)
                .build();

        when(shiftAssignmentRepository.findAllByAssignedDate(date)).thenReturn(List.of(sa));
        when(recordRepository.findAllByWorkDate(date)).thenReturn(List.of(unclosed));

        recordService.sweepDailyAttendanceRecords(date);

        assertEquals(AttendanceStatus.MISSING_CHECK_OUT, unclosed.getStatus());
        assertEquals(BigDecimal.ZERO, unclosed.getActualWorkUnits());
        verify(recordRepository, atLeastOnce()).saveAll(any());
    }
}
