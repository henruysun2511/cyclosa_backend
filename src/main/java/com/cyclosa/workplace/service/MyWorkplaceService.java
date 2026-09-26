package com.cyclosa.workplace.service;

import com.cyclosa.common.exception.AppException;
import com.cyclosa.workplace.dto.request.CreateHotdeskBookingRequest;
import com.cyclosa.workplace.dto.request.CreateIssueTicketRequest;
import com.cyclosa.workplace.dto.request.CreateRoomBookingRequest;
import com.cyclosa.workplace.dto.response.*;
import com.cyclosa.workplace.entity.WorkplaceSeat;
import com.cyclosa.workplace.exception.WorkplaceErrorCode;
import com.cyclosa.workplace.repository.WorkplaceSeatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MyWorkplaceService {

    private final WorkplaceSeatRepository seatRepository;
    private final WorkplaceSeatService seatService;
    private final WorkplaceBookingService bookingService;
    private final WorkplaceIssueTicketService issueTicketService;
    private final WorkplaceSecurityContext securityContext;

    @Transactional(readOnly = true)
    public WorkplaceSeatDetailResponse getMyAssignedSeat() {
        UUID companyId = securityContext.getCurrentCompanyId();
        UUID employeeId = securityContext.getRequiredEmployeeId();

        if (employeeId == null) {
            throw new AppException(WorkplaceErrorCode.EMPLOYEE_NOT_SPECIFIED);
        }

        WorkplaceSeat seat = (companyId != null)
                ? seatRepository.findByCompanyIdAndAssignedEmployeeId(companyId, employeeId).orElse(null)
                : null;

        if (seat == null) {
            throw new AppException(WorkplaceErrorCode.EMPLOYEE_HAS_NO_ASSIGNED_SEAT);
        }

        return seatService.getSeatDetail(seat.getId());
    }

    public List<HotdeskBookingResponse> getMyHotdeskBookings() {
        return bookingService.getMyHotdeskBookings();
    }

    public HotdeskBookingResponse bookHotdesk(CreateHotdeskBookingRequest request) {
        UUID employeeId = securityContext.getRequiredEmployeeId();
        request.setEmployeeId(employeeId);
        return bookingService.createHotdeskBooking(request);
    }

    public HotdeskBookingResponse cancelMyHotdeskBooking(UUID bookingId) {
        return bookingService.cancelHotdeskBooking(bookingId);
    }

    public HotdeskBookingResponse checkInMyHotdeskBooking(UUID bookingId) {
        return bookingService.checkInHotdeskBooking(bookingId);
    }

    public List<RoomBookingResponse> getMyRoomBookings() {
        return bookingService.getMyRoomBookings();
    }

    public RoomBookingResponse bookRoom(CreateRoomBookingRequest request) {
        UUID employeeId = securityContext.getRequiredEmployeeId();
        request.setBookedByEmployeeId(employeeId);
        return bookingService.createRoomBooking(request);
    }

    public RoomBookingResponse cancelMyRoomBooking(UUID bookingId) {
        return bookingService.cancelRoomBooking(bookingId);
    }

    public RoomBookingResponse checkInMyRoomBooking(UUID bookingId) {
        return bookingService.checkInRoomBooking(bookingId);
    }

    public IssueTicketResponse reportIssue(CreateIssueTicketRequest request) {
        return issueTicketService.createIssueTicket(request);
    }

    public List<IssueTicketResponse> getMyReportedTickets() {
        return issueTicketService.getMyReportedTickets();
    }
}
