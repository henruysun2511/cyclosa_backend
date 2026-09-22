package com.cyclosa.attendance;

import com.cyclosa.attendance.dto.request.BatchShiftAssignmentRequest;
import com.cyclosa.attendance.dto.response.ShiftAssignmentResponse;
import com.cyclosa.attendance.entity.Shift;
import com.cyclosa.attendance.entity.ShiftAssignment;
import com.cyclosa.attendance.enums.ShiftAssignmentStatus;
import com.cyclosa.attendance.mapper.ShiftAssignmentMapper;
import com.cyclosa.attendance.mapper.ShiftMapper;
import com.cyclosa.attendance.repository.ShiftAssignmentRepository;
import com.cyclosa.attendance.service.ShiftAssignmentService;
import com.cyclosa.attendance.service.ShiftService;
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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShiftAssignmentServiceTest {

    @Mock
    private ShiftAssignmentRepository assignmentRepository;

    @Mock
    private ShiftService shiftService;

    @Mock
    private EmployeeService employeeService;

    @Mock
    private SecurityPermissionEvaluator permEvaluator;

    @Spy
    private ShiftMapper shiftMapper = Mappers.getMapper(ShiftMapper.class);

    @Spy
    private ShiftAssignmentMapper assignmentMapper = Mappers.getMapper(ShiftAssignmentMapper.class);

    @InjectMocks
    private ShiftAssignmentService assignmentService;

    private UUID companyId;
    private UUID employeeId;
    private Shift shift;

    @BeforeEach
    void setUp() {
        org.springframework.test.util.ReflectionTestUtils.setField(assignmentMapper, "shiftMapper", shiftMapper);
        companyId = UUID.randomUUID();
        employeeId = UUID.randomUUID();
        shift = Shift.builder()
                .companyId(companyId)
                .code("CA_HC")
                .name("Ca Hành Chính")
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(17, 30))
                .build();
        shift.setId(UUID.randomUUID());
    }

    @Test
    @DisplayName("Phân ca hàng loạt theo khoảng ngày và ngày trong tuần")
    void testBatchAssignShifts() {
        LocalDate from = LocalDate.of(2026, 10, 5); // Monday
        LocalDate to = LocalDate.of(2026, 10, 9);   // Friday

        BatchShiftAssignmentRequest req = BatchShiftAssignmentRequest.builder()
                .shiftId(shift.getId())
                .employeeIds(Set.of(employeeId))
                .fromDate(from)
                .toDate(to)
                .daysOfWeek(List.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY))
                .note("Ca tháng 10")
                .build();

        when(shiftService.findShiftEntity(companyId, shift.getId())).thenReturn(shift);
        when(assignmentRepository.findByCompanyIdAndEmployeeIdAndAssignedDateWithShift(any(), any(), any()))
                .thenReturn(Optional.empty());
        when(assignmentRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(employeeService.getEmployeeSummaries(any())).thenReturn(Map.of(
                employeeId, EmployeeSummary.builder().id(employeeId).fullName("Nguyễn Văn A").build()
        ));

        List<ShiftAssignmentResponse> result = assignmentService.batchAssignShifts(companyId, req);

        assertNotNull(result);
        assertEquals(3, result.size()); // Mon, Wed, Fri
        verify(assignmentRepository).saveAll(any());
    }

    @Test
    @DisplayName("Xem lịch phân ca cá nhân của nhân sự")
    void testGetMySchedule() {
        UUID currentUserId = UUID.randomUUID();
        LocalDate start = LocalDate.of(2026, 10, 1);
        LocalDate end = LocalDate.of(2026, 10, 31);

        try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
            securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(currentUserId);
            when(employeeService.findEmployeeIdByUserId(currentUserId)).thenReturn(Optional.of(employeeId));

            ShiftAssignment sa = ShiftAssignment.builder()
                    .companyId(companyId)
                    .employeeId(employeeId)
                    .shift(shift)
                    .assignedDate(LocalDate.of(2026, 10, 5))
                    .status(ShiftAssignmentStatus.ASSIGNED)
                    .build();
            sa.setId(UUID.randomUUID());

            when(assignmentRepository.findByEmployeeIdAndDateRangeWithShift(employeeId, start, end))
                    .thenReturn(List.of(sa));
            when(employeeService.getEmployeeSummary(employeeId))
                    .thenReturn(EmployeeSummary.builder().id(employeeId).fullName("Nguyễn Văn A").build());

            List<ShiftAssignmentResponse> responses = assignmentService.getMySchedule(start, end);

            assertNotNull(responses);
            assertEquals(1, responses.size());
            assertEquals(sa.getId(), responses.get(0).getId());
        }
    }
}
