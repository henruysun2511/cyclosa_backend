package com.cyclosa.workplace.service;

import com.cyclosa.common.dto.summary.EmployeeSummary;
import com.cyclosa.common.exception.AppException;
import com.cyclosa.employee.service.EmployeeService;
import com.cyclosa.workplace.dto.response.ColleagueLocationResponse;
import com.cyclosa.workplace.dto.response.WorkplaceOccupancyReportResponse;
import com.cyclosa.workplace.dto.response.WorkplaceOccupancyReportResponse.FloorOccupancyStats;
import com.cyclosa.workplace.entity.WorkplaceFloor;
import com.cyclosa.workplace.entity.WorkplaceSeat;
import com.cyclosa.workplace.enums.SeatStatus;
import com.cyclosa.workplace.exception.WorkplaceErrorCode;
import com.cyclosa.workplace.repository.WorkplaceFloorRepository;
import com.cyclosa.workplace.repository.WorkplaceSeatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkplaceSearchService {

    private final WorkplaceSeatRepository seatRepository;
    private final WorkplaceFloorRepository floorRepository;
    private final EmployeeService employeeService;
    private final WorkplaceSecurityContext securityContext;

    @Transactional(readOnly = true)
    public List<ColleagueLocationResponse> findColleagueLocation(String query) {
        UUID companyId = securityContext.getCurrentCompanyId();

        List<WorkplaceSeat> assignedSeats = (companyId != null)
                ? seatRepository.findAllAssignedSeatsWithHierarchy(companyId)
                : Collections.emptyList();

        if (assignedSeats.isEmpty()) {
            return Collections.emptyList();
        }

        // Batch resolve EmployeeSummary (Principle 8 - Anti N+1)
        Set<UUID> empIds = assignedSeats.stream()
                .map(WorkplaceSeat::getAssignedEmployeeId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<UUID, EmployeeSummary> empMap = employeeService.getEmployeeSummaries(empIds);

        String trimmedQuery = (query != null) ? query.trim().toLowerCase() : "";

        List<ColleagueLocationResponse> result = new ArrayList<>();
        for (WorkplaceSeat seat : assignedSeats) {
            EmployeeSummary emp = empMap.get(seat.getAssignedEmployeeId());
            if (emp == null) continue;

            // Nếu có keyword tìm kiếm thì so sánh với Tên nhân viên, Mã nhân viên, hoặc Mã bàn
            if (!trimmedQuery.isEmpty()) {
                boolean matchName = emp.getFullName() != null && emp.getFullName().toLowerCase().contains(trimmedQuery);
                boolean matchCode = emp.getEmployeeCode() != null && emp.getEmployeeCode().toLowerCase().contains(trimmedQuery);
                boolean matchSeat = seat.getSeatCode() != null && seat.getSeatCode().toLowerCase().contains(trimmedQuery);

                if (!matchName && !matchCode && !matchSeat) {
                    continue;
                }
            }

            result.add(ColleagueLocationResponse.builder()
                    .employeeId(emp.getId())
                    .employee(emp)
                    .siteId(seat.getRow().getZone().getFloor().getSite().getId())
                    .siteName(seat.getRow().getZone().getFloor().getSite().getName())
                    .floorId(seat.getRow().getZone().getFloor().getId())
                    .floorNumber(seat.getRow().getZone().getFloor().getFloorNumber())
                    .zoneId(seat.getRow().getZone().getId())
                    .zoneName(seat.getRow().getZone().getName())
                    .rowId(seat.getRow().getId())
                    .rowName(seat.getRow().getName())
                    .seatId(seat.getId())
                    .seatCode(seat.getSeatCode())
                    .positionX(seat.getPositionX())
                    .positionY(seat.getPositionY())
                    .build());
        }

        return result;
    }

    @Transactional(readOnly = true)
    public ColleagueLocationResponse findColleagueLocationByEmployeeId(UUID employeeId) {
        UUID companyId = securityContext.getCurrentCompanyId();

        WorkplaceSeat seat = (companyId != null)
                ? seatRepository.findByCompanyIdAndAssignedEmployeeId(companyId, employeeId).orElse(null)
                : null;

        if (seat == null) {
            throw new AppException(WorkplaceErrorCode.EMPLOYEE_HAS_NO_ASSIGNED_SEAT);
        }

        // Hierarchy
        WorkplaceSeat seatWithHierarchy = seatRepository.findByIdWithHierarchy(seat.getId(), seat.getCompanyId())
                .orElse(seat);

        Map<UUID, EmployeeSummary> empMap = employeeService.getEmployeeSummaries(Set.of(employeeId));
        EmployeeSummary emp = empMap.get(employeeId);

        return ColleagueLocationResponse.builder()
                .employeeId(employeeId)
                .employee(emp)
                .siteId(seatWithHierarchy.getRow().getZone().getFloor().getSite().getId())
                .siteName(seatWithHierarchy.getRow().getZone().getFloor().getSite().getName())
                .floorId(seatWithHierarchy.getRow().getZone().getFloor().getId())
                .floorNumber(seatWithHierarchy.getRow().getZone().getFloor().getFloorNumber())
                .zoneId(seatWithHierarchy.getRow().getZone().getId())
                .zoneName(seatWithHierarchy.getRow().getZone().getName())
                .rowId(seatWithHierarchy.getRow().getId())
                .rowName(seatWithHierarchy.getRow().getName())
                .seatId(seatWithHierarchy.getId())
                .seatCode(seatWithHierarchy.getSeatCode())
                .positionX(seatWithHierarchy.getPositionX())
                .positionY(seatWithHierarchy.getPositionY())
                .build();
    }

    @Transactional(readOnly = true)
    public WorkplaceOccupancyReportResponse generateOccupancyReport(UUID siteId) {
        UUID companyId = securityContext.getCurrentCompanyId();
        if (companyId == null) {
            return WorkplaceOccupancyReportResponse.builder().floorStats(Collections.emptyList()).build();
        }

        long totalSeats = seatRepository.countByCompanyId(companyId);
        long occupiedSeats = seatRepository.countByCompanyIdAndStatus(companyId, SeatStatus.OCCUPIED);
        long availableSeats = seatRepository.countByCompanyIdAndStatus(companyId, SeatStatus.AVAILABLE);
        long maintenanceSeats = seatRepository.countByCompanyIdAndStatus(companyId, SeatStatus.MAINTENANCE);
        long blockedSeats = seatRepository.countByCompanyIdAndStatus(companyId, SeatStatus.BLOCKED);

        BigDecimal occupancyRate = BigDecimal.ZERO;
        if (totalSeats > 0) {
            occupancyRate = BigDecimal.valueOf(occupiedSeats)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalSeats), 2, RoundingMode.HALF_UP);
        }

        // Thống kê chi tiết theo từng tầng
        List<WorkplaceFloor> floors;
        if (siteId != null) {
            floors = floorRepository.findBySiteIdOrderByFloorNumberAsc(siteId);
        } else {
            floors = floorRepository.findAll();
        }

        List<FloorOccupancyStats> floorStatsList = new ArrayList<>();
        for (WorkplaceFloor floor : floors) {
            long floorTotal = seatRepository.countByCompanyIdAndRowZoneFloorId(companyId, floor.getId());
            long floorOccupied = seatRepository.countByCompanyIdAndRowZoneFloorIdAndStatus(companyId, floor.getId(), SeatStatus.OCCUPIED);
            long floorAvailable = seatRepository.countByCompanyIdAndRowZoneFloorIdAndStatus(companyId, floor.getId(), SeatStatus.AVAILABLE);

            BigDecimal floorRate = BigDecimal.ZERO;
            if (floorTotal > 0) {
                floorRate = BigDecimal.valueOf(floorOccupied)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(floorTotal), 2, RoundingMode.HALF_UP);
            }

            floorStatsList.add(FloorOccupancyStats.builder()
                    .floorId(floor.getId())
                    .floorNumber(floor.getFloorNumber())
                    .siteName(floor.getSite() != null ? floor.getSite().getName() : "")
                    .totalSeats(floorTotal)
                    .occupiedSeats(floorOccupied)
                    .availableSeats(floorAvailable)
                    .occupancyRatePercentage(floorRate)
                    .build());
        }

        return WorkplaceOccupancyReportResponse.builder()
                .totalSeats(totalSeats)
                .occupiedSeats(occupiedSeats)
                .availableSeats(availableSeats)
                .maintenanceSeats(maintenanceSeats)
                .blockedSeats(blockedSeats)
                .occupancyRatePercentage(occupancyRate)
                .floorStats(floorStatsList)
                .build();
    }
}
