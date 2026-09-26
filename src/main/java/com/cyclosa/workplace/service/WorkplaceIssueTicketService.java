package com.cyclosa.workplace.service;

import com.cyclosa.common.exception.AppException;
import com.cyclosa.common.response.PageData;
import com.cyclosa.common.dto.summary.EmployeeSummary;
import com.cyclosa.employee.service.EmployeeService;
import com.cyclosa.workplace.dto.filter.IssueTicketFilter;
import com.cyclosa.workplace.dto.request.CreateIssueTicketRequest;
import com.cyclosa.workplace.dto.request.UpdateIssueTicketStatusRequest;
import com.cyclosa.workplace.dto.response.IssueTicketResponse;
import com.cyclosa.workplace.entity.*;
import com.cyclosa.workplace.enums.IssueSeverity;
import com.cyclosa.workplace.enums.IssueStatus;
import com.cyclosa.workplace.enums.SeatStatus;
import com.cyclosa.workplace.exception.WorkplaceErrorCode;
import com.cyclosa.workplace.mapper.WorkplaceMapper;
import com.cyclosa.workplace.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkplaceIssueTicketService {

    private static final Set<String> ALLOWED_TICKET_SORT_FIELDS = Set.of(
            "createdAt", "severity", "status", "issueType"
    );

    private final WorkplaceIssueTicketRepository issueTicketRepository;
    private final WorkplaceSeatRepository seatRepository;
    private final WorkplaceNonDeskObjectRepository nonDeskObjectRepository;
    private final WorkplaceFloorRepository floorRepository;
    private final EmployeeService employeeService;
    private final WorkplaceMapper workplaceMapper;
    private final WorkplaceSecurityContext securityContext;

    @Transactional
    public IssueTicketResponse createIssueTicket(CreateIssueTicketRequest request) {
        UUID currentEmployeeId = securityContext.getRequiredEmployeeId();

        if (currentEmployeeId == null) {
            throw new AppException(WorkplaceErrorCode.EMPLOYEE_NOT_SPECIFIED);
        }

        WorkplaceFloor floor = floorRepository.findById(request.getFloorId())
                .orElseThrow(() -> new AppException(WorkplaceErrorCode.FLOOR_NOT_FOUND));

        UUID companyId = floor.getSite().getCompanyId();
        if (companyId == null) {
            companyId = securityContext.getCurrentCompanyId();
        }

        WorkplaceSeat seat = null;
        if (request.getSeatId() != null) {
            seat = seatRepository.findById(request.getSeatId())
                    .orElseThrow(() -> new AppException(WorkplaceErrorCode.SEAT_NOT_FOUND));
        }

        WorkplaceNonDeskObject object = null;
        if (request.getNonDeskObjectId() != null) {
            object = nonDeskObjectRepository.findById(request.getNonDeskObjectId())
                    .orElseThrow(() -> new AppException(WorkplaceErrorCode.NON_DESK_OBJECT_NOT_FOUND));
        }

        // Tự động điền tọa độ nếu người dùng không truyền tọa độ tự do
        var posX = request.getPositionX();
        var posY = request.getPositionY();
        if (posX == null && seat != null) {
            posX = seat.getPositionX();
            posY = seat.getPositionY();
        } else if (posX == null && object != null) {
            posX = object.getPositionX();
            posY = object.getPositionY();
        }

        IssueSeverity severity = request.getSeverity() != null ? request.getSeverity() : IssueSeverity.MEDIUM;

        WorkplaceIssueTicket ticket = WorkplaceIssueTicket.builder()
                .companyId(companyId)
                .seat(seat)
                .nonDeskObject(object)
                .floor(floor)
                .reportedByEmployeeId(currentEmployeeId)
                .issueType(request.getIssueType())
                .severity(severity)
                .title(request.getTitle().trim())
                .description(request.getDescription())
                .positionX(posX)
                .positionY(posY)
                .status(IssueStatus.OPEN)
                .build();

        ticket = issueTicketRepository.save(ticket);
        log.info("Facility issue ticket created: id={}, type={}, severity={}",
                ticket.getId(), ticket.getIssueType(), ticket.getSeverity());

        // Nếu sự cố tại bàn ở mức nghiêm trọng (HIGH/CRITICAL), tạm thời đánh dấu bàn là MAINTENANCE
        if (seat != null && (severity == IssueSeverity.HIGH || severity == IssueSeverity.CRITICAL)) {
            seat.setStatus(SeatStatus.MAINTENANCE);
            seatRepository.save(seat);
            log.warn("Seat {} set to MAINTENANCE due to high-severity issue ticket {}", seat.getSeatCode(), ticket.getId());
        }

        IssueTicketResponse response = workplaceMapper.toIssueTicketResponse(ticket);
        enrichTicketResponses(List.of(response));
        return response;
    }

    @Transactional
    public IssueTicketResponse updateTicketStatus(UUID ticketId, UpdateIssueTicketStatusRequest request) {
        WorkplaceIssueTicket ticket = issueTicketRepository.findById(ticketId)
                .orElseThrow(() -> new AppException(WorkplaceErrorCode.ISSUE_TICKET_NOT_FOUND));

        IssueStatus oldStatus = ticket.getStatus();
        IssueStatus newStatus = request.getStatus();

        ticket.setStatus(newStatus);
        if (request.getAssignedToUserId() != null) {
            ticket.setAssignedToUserId(request.getAssignedToUserId());
        }
        if (request.getResolutionNotes() != null) {
            ticket.setResolutionNotes(request.getResolutionNotes().trim());
        }

        if (newStatus == IssueStatus.RESOLVED || newStatus == IssueStatus.CLOSED) {
            ticket.setResolvedAt(LocalDateTime.now());

            // Tự động khôi phục trạng thái bàn nếu không còn sự cố nào khác đang mở
            if (ticket.getSeat() != null) {
                WorkplaceSeat seat = ticket.getSeat();
                UUID companyId = ticket.getCompanyId();
                boolean hasOtherActiveTickets = issueTicketRepository.hasActiveTicketsForSeat(
                        companyId, seat.getId(), List.of(IssueStatus.OPEN, IssueStatus.IN_PROGRESS), ticket.getId()
                );

                if (!hasOtherActiveTickets && seat.getStatus() == SeatStatus.MAINTENANCE) {
                    seat.setStatus(seat.getAssignedEmployeeId() != null ? SeatStatus.OCCUPIED : SeatStatus.AVAILABLE);
                    seatRepository.save(seat);
                    log.info("Seat {} restored to {} after ticket {} resolution",
                            seat.getSeatCode(), seat.getStatus(), ticket.getId());
                }
            }
        }

        ticket = issueTicketRepository.save(ticket);
        log.info("Ticket {} status changed from {} to {}", ticketId, oldStatus, newStatus);

        IssueTicketResponse response = workplaceMapper.toIssueTicketResponse(ticket);
        enrichTicketResponses(List.of(response));
        return response;
    }

    @Transactional(readOnly = true)
    public PageData<IssueTicketResponse> filterTickets(IssueTicketFilter filter) {
        UUID companyId = securityContext.getCurrentCompanyId();
        Pageable pageable = filter.toPageable("createdAt", ALLOWED_TICKET_SORT_FIELDS);

        Page<WorkplaceIssueTicket> page = issueTicketRepository.filterTickets(
                companyId,
                filter.getFloorId(),
                filter.getIssueType(),
                filter.getSeverity(),
                filter.getStatus(),
                filter.getReportedByEmployeeId(),
                pageable
        );

        List<IssueTicketResponse> responses = page.getContent().stream()
                .map(workplaceMapper::toIssueTicketResponse)
                .collect(Collectors.toList());

        enrichTicketResponses(responses);
        return PageData.of(page, responses);
    }

    @Transactional(readOnly = true)
    public List<IssueTicketResponse> getMyReportedTickets() {
        UUID companyId = securityContext.getCurrentCompanyId();
        UUID employeeId = securityContext.getRequiredEmployeeId();
        if (employeeId == null) {
            return Collections.emptyList();
        }

        List<WorkplaceIssueTicket> tickets = issueTicketRepository.findByCompanyIdAndReportedByEmployeeIdOrderByCreatedAtDesc(companyId, employeeId);
        List<IssueTicketResponse> responses = tickets.stream()
                .map(workplaceMapper::toIssueTicketResponse)
                .collect(Collectors.toList());

        enrichTicketResponses(responses);
        return responses;
    }

    @Transactional(readOnly = true)
    public IssueTicketResponse getTicketDetail(UUID ticketId) {
        WorkplaceIssueTicket ticket = issueTicketRepository.findById(ticketId)
                .orElseThrow(() -> new AppException(WorkplaceErrorCode.ISSUE_TICKET_NOT_FOUND));

        IssueTicketResponse response = workplaceMapper.toIssueTicketResponse(ticket);
        enrichTicketResponses(List.of(response));
        return response;
    }

    private void enrichTicketResponses(List<IssueTicketResponse> responses) {
        if (responses == null || responses.isEmpty()) return;
        Set<UUID> empIds = responses.stream()
                .map(IssueTicketResponse::getReportedByEmployeeId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (empIds.isEmpty()) return;
        Map<UUID, EmployeeSummary> empMap = employeeService.getEmployeeSummaries(empIds);
        responses.forEach(r -> {
            if (r.getReportedByEmployeeId() != null) {
                r.setReportedByEmployee(empMap.get(r.getReportedByEmployeeId()));
            }
        });
    }
}
