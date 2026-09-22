package com.cyclosa.attendance;

import com.cyclosa.attendance.dto.request.CreateShiftRequest;
import com.cyclosa.attendance.dto.response.ShiftResponse;
import com.cyclosa.attendance.entity.Shift;
import com.cyclosa.attendance.exception.AttendanceErrorCode;
import com.cyclosa.attendance.mapper.ShiftMapper;
import com.cyclosa.attendance.repository.ShiftAssignmentRepository;
import com.cyclosa.attendance.repository.ShiftRepository;
import com.cyclosa.attendance.service.ShiftService;
import com.cyclosa.common.exception.AppException;
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
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShiftServiceTest {

    @Mock
    private ShiftRepository shiftRepository;

    @Mock
    private ShiftAssignmentRepository shiftAssignmentRepository;

    @Spy
    private ShiftMapper shiftMapper = Mappers.getMapper(ShiftMapper.class);

    @InjectMocks
    private ShiftService shiftService;

    private UUID companyId;
    private Shift shift;

    @BeforeEach
    void setUp() {
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
                .isActive(true)
                .build();
        shift.setId(UUID.randomUUID());
    }

    @Test
    @DisplayName("Tạo ca làm việc thành công")
    void testCreateShiftSuccess() {
        CreateShiftRequest req = CreateShiftRequest.builder()
                .code("CA_HC")
                .name("Ca Hành Chính")
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(17, 30))
                .build();

        when(shiftRepository.existsByCodeAndCompanyId("CA_HC", companyId)).thenReturn(false);
        when(shiftRepository.save(any(Shift.class))).thenReturn(shift);

        ShiftResponse res = shiftService.createShift(companyId, req);

        assertNotNull(res);
        assertEquals("CA_HC", res.getCode());
        assertEquals("Ca Hành Chính", res.getName());
        verify(shiftRepository).save(any(Shift.class));
    }

    @Test
    @DisplayName("Ném lỗi khi mã ca làm việc đã tồn tại")
    void testCreateShiftDuplicateCode() {
        CreateShiftRequest req = CreateShiftRequest.builder()
                .code("CA_HC")
                .name("Ca Hành Chính")
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(17, 30))
                .build();

        when(shiftRepository.existsByCodeAndCompanyId("CA_HC", companyId)).thenReturn(true);

        AppException ex = assertThrows(AppException.class, () -> shiftService.createShift(companyId, req));
        assertEquals(AttendanceErrorCode.SHIFT_CODE_EXISTS.getCode(), ex.getErrorCode().getCode());
        verify(shiftRepository, never()).save(any(Shift.class));
    }

    @Test
    @DisplayName("Ném lỗi khi xóa ca làm việc đang được phân ca cho nhân viên")
    void testDeleteShiftHasAssignments() {
        UUID shiftId = shift.getId();
        when(shiftRepository.findById(shiftId)).thenReturn(Optional.of(shift));
        when(shiftAssignmentRepository.existsByShiftId(shiftId)).thenReturn(true);

        AppException ex = assertThrows(AppException.class, () -> shiftService.deleteShift(companyId, shiftId));
        assertEquals(AttendanceErrorCode.SHIFT_HAS_ASSIGNMENTS.getCode(), ex.getErrorCode().getCode());
        verify(shiftRepository, never()).delete(any(Shift.class));
    }

    @Test
    @DisplayName("Ném lỗi khi ca ngày có giờ kết thúc trước giờ bắt đầu")
    void testCreateShiftInvalidTime() {
        CreateShiftRequest req = CreateShiftRequest.builder()
                .code("CA_SAI")
                .name("Ca Sai Giờ")
                .startTime(LocalTime.of(17, 30))
                .endTime(LocalTime.of(8, 0))
                .isNightShift(false)
                .build();

        assertThrows(AppException.class, () -> shiftService.createShift(companyId, req));
    }

    @Test
    @DisplayName("Ném lỗi khi giờ nghỉ giữa ca không hợp lệ")
    void testCreateShiftInvalidBreakTime() {
        CreateShiftRequest req = CreateShiftRequest.builder()
                .code("CA_SAI_NGHI")
                .name("Ca Sai Giờ Nghỉ")
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(17, 30))
                .breakStartTime(LocalTime.of(13, 30))
                .breakEndTime(LocalTime.of(12, 0))
                .build();

        assertThrows(AppException.class, () -> shiftService.createShift(companyId, req));
    }
}
