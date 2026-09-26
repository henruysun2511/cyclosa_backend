package com.cyclosa.workplace.service;

import com.cyclosa.common.exception.AppException;
import com.cyclosa.common.response.PageData;
import com.cyclosa.common.dto.summary.EmployeeSummary;
import com.cyclosa.employee.service.EmployeeService;
import com.cyclosa.workplace.dto.filter.HotdeskBookingFilter;
import com.cyclosa.workplace.dto.filter.RoomBookingFilter;
import com.cyclosa.workplace.dto.request.CreateHotdeskBookingRequest;
import com.cyclosa.workplace.dto.request.CreateRoomBookingRequest;
import com.cyclosa.workplace.dto.response.HotdeskBookingResponse;
import com.cyclosa.workplace.dto.response.RoomAvailabilityResponse;
import com.cyclosa.workplace.dto.response.RoomBookingResponse;
import com.cyclosa.workplace.entity.*;
import com.cyclosa.workplace.enums.*;
import com.cyclosa.workplace.exception.WorkplaceErrorCode;
import com.cyclosa.workplace.mapper.WorkplaceMapper;
import com.cyclosa.workplace.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkplaceBookingService {

    private static final Set<String> ALLOWED_BOOKING_SORT_FIELDS = Set.of(
            "createdAt", "bookingDate", "status", "startTime", "endTime"
    );

    private final WorkplaceHotdeskBookingRepository hotdeskBookingRepository;
    private final WorkplaceRoomBookingRepository roomBookingRepository;
    private final WorkplaceSeatRepository seatRepository;
    private final WorkplaceNonDeskObjectRepository nonDeskObjectRepository;
    private final EmployeeService employeeService;
    private final WorkplaceMapper workplaceMapper;
    private final WorkplaceSecurityContext securityContext;

    // =========================================================================
    // HOTDESK BOOKING
    // =========================================================================

    @Transactional
    public HotdeskBookingResponse createHotdeskBooking(CreateHotdeskBookingRequest request) {
        UUID companyId = securityContext.getCurrentCompanyId();
        UUID currentEmployeeId = securityContext.getRequiredEmployeeId();

        UUID employeeId = request.getEmployeeId() != null ? request.getEmployeeId() : currentEmployeeId;
        if (employeeId == null) {
            throw new AppException(WorkplaceErrorCode.EMPLOYEE_NOT_SPECIFIED);
        }

        LocalDate bookingDate = request.getBookingDate() != null ? request.getBookingDate() : LocalDate.now();

        // 1. Kiểm tra ghế tồn tại và là dạng HOT_DESK
        WorkplaceSeat seat = seatRepository.findById(request.getSeatId())
                .orElseThrow(() -> new AppException(WorkplaceErrorCode.SEAT_NOT_FOUND));

        if (seat.getSeatType() != SeatType.HOT_DESK) {
            throw new AppException(WorkplaceErrorCode.SEAT_NOT_AVAILABLE_FOR_HOTDESK);
        }

        if (seat.getStatus() == SeatStatus.MAINTENANCE || seat.getStatus() == SeatStatus.BLOCKED) {
            throw new AppException(WorkplaceErrorCode.SEAT_IN_MAINTENANCE);
        }

        if (companyId == null) {
            companyId = seat.getCompanyId();
        }

        // 2. Kiểm tra nhân viên đã có booking active trong ngày này chưa
        boolean alreadyBooked = hotdeskBookingRepository.existsByCompanyIdAndEmployeeIdAndBookingDateAndStatusIn(
                companyId, employeeId, bookingDate, List.of(HotdeskBookingStatus.CONFIRMED, HotdeskBookingStatus.CHECKED_IN)
        );
        if (alreadyBooked) {
            throw new AppException(WorkplaceErrorCode.HOTDESK_ALREADY_BOOKED_FOR_DATE);
        }

        // 3. Kiểm tra ghế đã bị ai khác đặt trong ngày này chưa
        boolean seatTaken = hotdeskBookingRepository.existsByCompanyIdAndSeatIdAndBookingDateAndStatusIn(
                companyId, seat.getId(), bookingDate, List.of(HotdeskBookingStatus.CONFIRMED, HotdeskBookingStatus.CHECKED_IN)
        );
        if (seatTaken) {
            throw new AppException(WorkplaceErrorCode.SEAT_ALREADY_OCCUPIED);
        }

        // 4. Tạo booking
        WorkplaceHotdeskBooking booking = WorkplaceHotdeskBooking.builder()
                .companyId(companyId)
                .seat(seat)
                .employeeId(employeeId)
                .bookingDate(bookingDate)
                .status(HotdeskBookingStatus.CONFIRMED)
                .notes(request.getNotes())
                .build();

        booking = hotdeskBookingRepository.save(booking);
        log.info("Hotdesk booked: id={}, seat={}, employee={}, date={}",
                booking.getId(), seat.getSeatCode(), employeeId, bookingDate);

        HotdeskBookingResponse response = workplaceMapper.toHotdeskBookingResponse(booking);
        enrichHotdeskResponses(List.of(response));
        return response;
    }

    @Transactional
    public HotdeskBookingResponse cancelHotdeskBooking(UUID bookingId) {
        WorkplaceHotdeskBooking booking = hotdeskBookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(WorkplaceErrorCode.HOTDESK_BOOKING_NOT_FOUND));

        if (booking.getStatus() == HotdeskBookingStatus.CANCELLED) {
            throw new AppException(WorkplaceErrorCode.BOOKING_ALREADY_CANCELLED);
        }

        booking.setStatus(HotdeskBookingStatus.CANCELLED);
        booking = hotdeskBookingRepository.save(booking);
        log.info("Hotdesk booking cancelled: id={}", bookingId);

        HotdeskBookingResponse response = workplaceMapper.toHotdeskBookingResponse(booking);
        enrichHotdeskResponses(List.of(response));
        return response;
    }

    @Transactional
    public HotdeskBookingResponse checkInHotdeskBooking(UUID bookingId) {
        WorkplaceHotdeskBooking booking = hotdeskBookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(WorkplaceErrorCode.HOTDESK_BOOKING_NOT_FOUND));

        if (booking.getStatus() != HotdeskBookingStatus.CONFIRMED) {
            throw new AppException(WorkplaceErrorCode.HOTDESK_CANNOT_CHECKIN);
        }

        booking.setStatus(HotdeskBookingStatus.CHECKED_IN);
        booking.setCheckedInAt(LocalDateTime.now());
        booking = hotdeskBookingRepository.save(booking);
        log.info("Hotdesk booking checked-in: id={}, time={}", bookingId, booking.getCheckedInAt());

        HotdeskBookingResponse response = workplaceMapper.toHotdeskBookingResponse(booking);
        enrichHotdeskResponses(List.of(response));
        return response;
    }

    @Transactional(readOnly = true)
    public PageData<HotdeskBookingResponse> filterHotdeskBookings(HotdeskBookingFilter filter) {
        UUID companyId = securityContext.getCurrentCompanyId();
        Pageable pageable = filter.toPageable("createdAt", ALLOWED_BOOKING_SORT_FIELDS);

        Page<WorkplaceHotdeskBooking> page = hotdeskBookingRepository.filterBookings(
                companyId,
                filter.getEmployeeId(),
                filter.getSeatId(),
                filter.getBookingDate(),
                filter.getStatus(),
                filter.getFloorId(),
                pageable
        );

        List<HotdeskBookingResponse> responses = page.getContent().stream()
                .map(workplaceMapper::toHotdeskBookingResponse)
                .collect(Collectors.toList());

        enrichHotdeskResponses(responses);
        return PageData.of(page, responses);
    }

    @Transactional(readOnly = true)
    public List<HotdeskBookingResponse> getMyHotdeskBookings() {
        UUID companyId = securityContext.getCurrentCompanyId();
        UUID employeeId = securityContext.getRequiredEmployeeId();
        if (employeeId == null) {
            return Collections.emptyList();
        }

        List<WorkplaceHotdeskBooking> bookings = hotdeskBookingRepository.findMyBookings(companyId, employeeId);
        List<HotdeskBookingResponse> responses = bookings.stream()
                .map(workplaceMapper::toHotdeskBookingResponse)
                .collect(Collectors.toList());

        enrichHotdeskResponses(responses);
        return responses;
    }

    // =========================================================================
    // ROOM BOOKING
    // =========================================================================

    @Transactional
    public RoomBookingResponse createRoomBooking(CreateRoomBookingRequest request) {
        UUID currentEmployeeId = securityContext.getRequiredEmployeeId();

        UUID bookedBy = request.getBookedByEmployeeId() != null ? request.getBookedByEmployeeId() : currentEmployeeId;
        if (bookedBy == null) {
            throw new AppException(WorkplaceErrorCode.EMPLOYEE_NOT_SPECIFIED);
        }

        if (request.getStartTime().isAfter(request.getEndTime()) || request.getStartTime().equals(request.getEndTime())) {
            throw new AppException(WorkplaceErrorCode.ROOM_BOOKING_INVALID_TIME);
        }

        // 1. Kiểm tra phòng họp
        WorkplaceNonDeskObject room = nonDeskObjectRepository.findById(request.getRoomId())
                .orElseThrow(() -> new AppException(WorkplaceErrorCode.ROOM_NOT_FOUND));

        if (room.getObjectType() != NonDeskObjectType.MEETING_ROOM || !Boolean.TRUE.equals(room.getIsBookable())) {
            throw new AppException(WorkplaceErrorCode.ROOM_NOT_BOOKABLE);
        }

        UUID companyId = room.getCompanyId();
        if (companyId == null) {
            companyId = securityContext.getCurrentCompanyId();
        }

        // 2. Chống trùng lịch (Conflict Detection)
        boolean hasOverlap = roomBookingRepository.hasOverlappingBooking(
                companyId,
                room.getId(),
                request.getBookingDate(),
                request.getStartTime(),
                request.getEndTime(),
                List.of(RoomBookingStatus.CONFIRMED, RoomBookingStatus.CHECKED_IN),
                null
        );

        if (hasOverlap) {
            throw new AppException(WorkplaceErrorCode.ROOM_TIME_CONFLICT);
        }

        // 3. Tạo booking
        WorkplaceRoomBooking booking = WorkplaceRoomBooking.builder()
                .companyId(companyId)
                .room(room)
                .bookedByEmployeeId(bookedBy)
                .meetingTitle(request.getMeetingTitle().trim())
                .bookingDate(request.getBookingDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .participantCount(request.getParticipantCount() != null ? request.getParticipantCount() : 1)
                .status(RoomBookingStatus.CONFIRMED)
                .notes(request.getNotes())
                .build();

        booking = roomBookingRepository.save(booking);
        log.info("Room booked: id={}, room={}, bookedBy={}, date={}, {}-{}",
                booking.getId(), room.getName(), bookedBy, request.getBookingDate(),
                request.getStartTime(), request.getEndTime());

        RoomBookingResponse response = workplaceMapper.toRoomBookingResponse(booking);
        enrichRoomResponses(List.of(response));
        return response;
    }

    @Transactional
    public RoomBookingResponse cancelRoomBooking(UUID bookingId) {
        WorkplaceRoomBooking booking = roomBookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(WorkplaceErrorCode.ROOM_BOOKING_NOT_FOUND));

        if (booking.getStatus() == RoomBookingStatus.CANCELLED) {
            throw new AppException(WorkplaceErrorCode.BOOKING_ALREADY_CANCELLED);
        }

        booking.setStatus(RoomBookingStatus.CANCELLED);
        booking = roomBookingRepository.save(booking);
        log.info("Room booking cancelled: id={}", bookingId);

        RoomBookingResponse response = workplaceMapper.toRoomBookingResponse(booking);
        enrichRoomResponses(List.of(response));
        return response;
    }

    @Transactional
    public RoomBookingResponse checkInRoomBooking(UUID bookingId) {
        WorkplaceRoomBooking booking = roomBookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(WorkplaceErrorCode.ROOM_BOOKING_NOT_FOUND));

        if (booking.getStatus() != RoomBookingStatus.CONFIRMED) {
            throw new AppException(WorkplaceErrorCode.ROOM_CANNOT_CHECKIN);
        }

        booking.setStatus(RoomBookingStatus.CHECKED_IN);
        booking = roomBookingRepository.save(booking);
        log.info("Room booking checked-in: id={}", bookingId);

        RoomBookingResponse response = workplaceMapper.toRoomBookingResponse(booking);
        enrichRoomResponses(List.of(response));
        return response;
    }

    @Transactional(readOnly = true)
    public RoomAvailabilityResponse getRoomAvailability(UUID roomId, LocalDate date) {
        WorkplaceNonDeskObject room = nonDeskObjectRepository.findById(roomId)
                .orElseThrow(() -> new AppException(WorkplaceErrorCode.ROOM_NOT_FOUND));

        UUID companyId = room.getCompanyId();
        if (companyId == null) {
            companyId = securityContext.getCurrentCompanyId();
        }

        LocalDate queryDate = date != null ? date : LocalDate.now();

        List<WorkplaceRoomBooking> bookings = roomBookingRepository.findBookingsByRoomAndDate(companyId, roomId, queryDate);

        // Batch enrich tên người đặt
        Set<UUID> empIds = bookings.stream().map(WorkplaceRoomBooking::getBookedByEmployeeId).collect(Collectors.toSet());
        Map<UUID, EmployeeSummary> empMap = employeeService.getEmployeeSummaries(empIds);

        List<RoomAvailabilityResponse.TimeSlot> bookedSlots = bookings.stream()
                .map(b -> {
                    EmployeeSummary emp = empMap.get(b.getBookedByEmployeeId());
                    String name = emp != null ? emp.getFullName() : "N/A";
                    return RoomAvailabilityResponse.TimeSlot.builder()
                            .bookingId(b.getId())
                            .meetingTitle(b.getMeetingTitle())
                            .startTime(b.getStartTime())
                            .endTime(b.getEndTime())
                            .bookedByName(name)
                            .build();
                })
                .collect(Collectors.toList());

        return RoomAvailabilityResponse.builder()
                .roomId(room.getId())
                .roomName(room.getName())
                .capacity(room.getMeetingRoomCapacity())
                .date(queryDate)
                .bookedSlots(bookedSlots)
                .build();
    }

    @Transactional(readOnly = true)
    public PageData<RoomBookingResponse> filterRoomBookings(RoomBookingFilter filter) {
        UUID companyId = securityContext.getCurrentCompanyId();
        Pageable pageable = filter.toPageable("createdAt", ALLOWED_BOOKING_SORT_FIELDS);

        Page<WorkplaceRoomBooking> page = roomBookingRepository.filterRoomBookings(
                companyId,
                filter.getRoomId(),
                filter.getBookingDate(),
                filter.getStatus(),
                filter.getBookedByEmployeeId(),
                pageable
        );

        List<RoomBookingResponse> responses = page.getContent().stream()
                .map(workplaceMapper::toRoomBookingResponse)
                .collect(Collectors.toList());

        enrichRoomResponses(responses);
        return PageData.of(page, responses);
    }

    @Transactional(readOnly = true)
    public List<RoomBookingResponse> getMyRoomBookings() {
        UUID companyId = securityContext.getCurrentCompanyId();
        UUID employeeId = securityContext.getRequiredEmployeeId();
        if (employeeId == null) {
            return Collections.emptyList();
        }

        List<WorkplaceRoomBooking> bookings = roomBookingRepository.findMyRoomBookings(companyId, employeeId);
        List<RoomBookingResponse> responses = bookings.stream()
                .map(workplaceMapper::toRoomBookingResponse)
                .collect(Collectors.toList());

        enrichRoomResponses(responses);
        return responses;
    }

    // =========================================================================
    // HELPER ENRICHMENT METHODS (Principle 8 - Anti N+1)
    // =========================================================================

    private void enrichHotdeskResponses(List<HotdeskBookingResponse> responses) {
        if (responses == null || responses.isEmpty()) return;
        Set<UUID> empIds = responses.stream()
                .map(HotdeskBookingResponse::getEmployeeId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (empIds.isEmpty()) return;
        Map<UUID, EmployeeSummary> empMap = employeeService.getEmployeeSummaries(empIds);
        responses.forEach(r -> {
            if (r.getEmployeeId() != null) {
                r.setEmployee(empMap.get(r.getEmployeeId()));
            }
        });
    }

    private void enrichRoomResponses(List<RoomBookingResponse> responses) {
        if (responses == null || responses.isEmpty()) return;
        Set<UUID> empIds = responses.stream()
                .map(RoomBookingResponse::getBookedByEmployeeId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (empIds.isEmpty()) return;
        Map<UUID, EmployeeSummary> empMap = employeeService.getEmployeeSummaries(empIds);
        responses.forEach(r -> {
            if (r.getBookedByEmployeeId() != null) {
                r.setBookedByEmployee(empMap.get(r.getBookedByEmployeeId()));
            }
        });
    }
}
