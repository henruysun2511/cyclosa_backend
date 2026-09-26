package com.cyclosa.workplace;

import com.cyclosa.common.exception.AppException;
import com.cyclosa.employee.service.EmployeeService;
import com.cyclosa.workplace.dto.request.CreateHotdeskBookingRequest;
import com.cyclosa.workplace.dto.request.CreateRoomBookingRequest;
import com.cyclosa.workplace.dto.response.HotdeskBookingResponse;
import com.cyclosa.workplace.dto.response.RoomBookingResponse;
import com.cyclosa.workplace.entity.*;
import com.cyclosa.workplace.enums.*;
import com.cyclosa.workplace.exception.WorkplaceErrorCode;
import com.cyclosa.workplace.mapper.WorkplaceMapper;
import com.cyclosa.workplace.repository.WorkplaceHotdeskBookingRepository;
import com.cyclosa.workplace.repository.WorkplaceNonDeskObjectRepository;
import com.cyclosa.workplace.repository.WorkplaceRoomBookingRepository;
import com.cyclosa.workplace.repository.WorkplaceSeatRepository;
import com.cyclosa.workplace.service.WorkplaceBookingService;
import com.cyclosa.workplace.service.WorkplaceSecurityContext;
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
import java.time.LocalTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkplaceBookingServiceTest {

    @Mock
    private WorkplaceHotdeskBookingRepository hotdeskBookingRepository;

    @Mock
    private WorkplaceRoomBookingRepository roomBookingRepository;

    @Mock
    private WorkplaceSeatRepository seatRepository;

    @Mock
    private WorkplaceNonDeskObjectRepository nonDeskObjectRepository;

    @Mock
    private EmployeeService employeeService;

    @Mock
    private WorkplaceSecurityContext securityContext;

    @Spy
    private WorkplaceMapper workplaceMapper = Mappers.getMapper(WorkplaceMapper.class);

    @InjectMocks
    private WorkplaceBookingService bookingService;

    private UUID companyId;
    private UUID employeeId;
    private UUID seatId;
    private UUID roomId;

    @BeforeEach
    void setUp() {
        companyId = UUID.randomUUID();
        employeeId = UUID.randomUUID();
        seatId = UUID.randomUUID();
        roomId = UUID.randomUUID();

        lenient().when(securityContext.getCurrentCompanyId()).thenReturn(companyId);
        lenient().when(securityContext.getRequiredEmployeeId()).thenReturn(employeeId);
    }

    @Test
    @DisplayName("Đặt chỗ ngồi Hot-desk thành công khi bàn hợp lệ và chưa ai đặt")
    void createHotdeskBooking_success() {
        WorkplaceSite site = WorkplaceSite.builder().code("S1").name("Site 1").build();
        site.setId(UUID.randomUUID());
        WorkplaceFloor floor = WorkplaceFloor.builder().site(site).floorNumber("Tầng 2").build();
        floor.setId(UUID.randomUUID());
        WorkplaceZone zone = WorkplaceZone.builder().floor(floor).name("Zone A").build();
        zone.setId(UUID.randomUUID());
        WorkplaceRow row = WorkplaceRow.builder().zone(zone).name("Row 1").build();
        row.setId(UUID.randomUUID());

        WorkplaceSeat seat = WorkplaceSeat.builder()
                .seatCode("HD-01")
                .seatType(SeatType.HOT_DESK)
                .status(SeatStatus.AVAILABLE)
                .companyId(companyId)
                .row(row)
                .build();
        seat.setId(seatId);

        LocalDate bookingDate = LocalDate.now().plusDays(1);
        CreateHotdeskBookingRequest request = CreateHotdeskBookingRequest.builder()
                .seatId(seatId)
                .bookingDate(bookingDate)
                .notes("Làm việc nửa ngày")
                .build();

        when(seatRepository.findById(seatId)).thenReturn(Optional.of(seat));
        when(hotdeskBookingRepository.existsByCompanyIdAndEmployeeIdAndBookingDateAndStatusIn(
                eq(companyId), eq(employeeId), eq(bookingDate), anyList()))
                .thenReturn(false);
        when(hotdeskBookingRepository.existsByCompanyIdAndSeatIdAndBookingDateAndStatusIn(
                eq(companyId), eq(seatId), eq(bookingDate), anyList()))
                .thenReturn(false);

        when(hotdeskBookingRepository.save(any(WorkplaceHotdeskBooking.class))).thenAnswer(invocation -> {
            WorkplaceHotdeskBooking b = invocation.getArgument(0);
            b.setId(UUID.randomUUID());
            return b;
        });

        HotdeskBookingResponse response = bookingService.createHotdeskBooking(request);

        assertThat(response).isNotNull();
        assertThat(response.getSeatCode()).isEqualTo("HD-01");
        assertThat(response.getStatus()).isEqualTo(HotdeskBookingStatus.CONFIRMED);
        verify(hotdeskBookingRepository, times(1)).save(any(WorkplaceHotdeskBooking.class));
    }

    @Test
    @DisplayName("Đặt chỗ Hot-desk thất bại khi bàn là loại DEDICATED (ghế cố định)")
    void createHotdeskBooking_notHotdesk_throwsException() {
        WorkplaceSeat seat = WorkplaceSeat.builder()
                .seatCode("DED-01")
                .seatType(SeatType.DEDICATED)
                .status(SeatStatus.AVAILABLE)
                .companyId(companyId)
                .build();
        seat.setId(seatId);

        CreateHotdeskBookingRequest request = CreateHotdeskBookingRequest.builder()
                .seatId(seatId)
                .bookingDate(LocalDate.now())
                .build();

        when(seatRepository.findById(seatId)).thenReturn(Optional.of(seat));

        assertThatThrownBy(() -> bookingService.createHotdeskBooking(request))
                .isInstanceOf(AppException.class)
                .hasMessageContaining(WorkplaceErrorCode.SEAT_NOT_AVAILABLE_FOR_HOTDESK.getMessage());
    }

    @Test
    @DisplayName("Đặt phòng họp thành công khi không có cuộc họp nào khác trùng giờ")
    void createRoomBooking_success() {
        WorkplaceSite site = WorkplaceSite.builder().code("S1").name("Site 1").build();
        site.setId(UUID.randomUUID());
        WorkplaceFloor floor = WorkplaceFloor.builder().site(site).floorNumber("Tầng 3").build();
        floor.setId(UUID.randomUUID());
        WorkplaceZone zone = WorkplaceZone.builder().floor(floor).name("Zone Meeting").build();
        zone.setId(UUID.randomUUID());

        WorkplaceNonDeskObject room = WorkplaceNonDeskObject.builder()
                .name("Phòng họp Sông Hồng")
                .objectType(NonDeskObjectType.MEETING_ROOM)
                .meetingRoomCapacity(10)
                .companyId(companyId)
                .zone(zone)
                .build();
        room.setId(roomId);

        LocalDate date = LocalDate.now().plusDays(2);
        LocalTime start = LocalTime.of(9, 0);
        LocalTime end = LocalTime.of(10, 30);

        CreateRoomBookingRequest request = CreateRoomBookingRequest.builder()
                .roomId(roomId)
                .meetingTitle("Họp Sprint Review Q3")
                .bookingDate(date)
                .startTime(start)
                .endTime(end)
                .participantCount(8)
                .build();

        when(nonDeskObjectRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(roomBookingRepository.hasOverlappingBooking(
                eq(companyId), eq(roomId), eq(date), eq(start), eq(end), anyList(), isNull()))
                .thenReturn(false);
        when(roomBookingRepository.save(any(WorkplaceRoomBooking.class))).thenAnswer(invocation -> {
            WorkplaceRoomBooking b = invocation.getArgument(0);
            b.setId(UUID.randomUUID());
            return b;
        });

        RoomBookingResponse response = bookingService.createRoomBooking(request);

        assertThat(response).isNotNull();
        assertThat(response.getMeetingTitle()).isEqualTo("Họp Sprint Review Q3");
        assertThat(response.getStatus()).isEqualTo(RoomBookingStatus.CONFIRMED);
        verify(roomBookingRepository, times(1)).save(any(WorkplaceRoomBooking.class));
    }

    @Test
    @DisplayName("Đặt phòng họp ném lỗi ROOM_TIME_CONFLICT khi khung giờ bị trùng lặp")
    void createRoomBooking_overlapConflict_throwsException() {
        WorkplaceNonDeskObject room = WorkplaceNonDeskObject.builder()
                .name("Phòng họp Sông Đà")
                .objectType(NonDeskObjectType.MEETING_ROOM)
                .companyId(companyId)
                .build();
        room.setId(roomId);

        LocalDate date = LocalDate.now().plusDays(2);
        LocalTime start = LocalTime.of(14, 0);
        LocalTime end = LocalTime.of(15, 0);

        CreateRoomBookingRequest request = CreateRoomBookingRequest.builder()
                .roomId(roomId)
                .meetingTitle("Họp Khẩn Cấp Ban Giám Đốc")
                .bookingDate(date)
                .startTime(start)
                .endTime(end)
                .build();

        when(nonDeskObjectRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(roomBookingRepository.hasOverlappingBooking(
                eq(companyId), eq(roomId), eq(date), eq(start), eq(end), anyList(), isNull()))
                .thenReturn(true);

        assertThatThrownBy(() -> bookingService.createRoomBooking(request))
                .isInstanceOf(AppException.class)
                .hasMessageContaining(WorkplaceErrorCode.ROOM_TIME_CONFLICT.getMessage());

        verify(roomBookingRepository, never()).save(any());
    }
}
