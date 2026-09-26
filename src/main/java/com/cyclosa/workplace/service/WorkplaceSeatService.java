package com.cyclosa.workplace.service;

import com.cyclosa.asset.dto.response.AssetDetailResponse;
import com.cyclosa.asset.service.AssetService;
import com.cyclosa.common.dto.summary.EmployeeSummary;
import com.cyclosa.common.exception.AppException;
import com.cyclosa.common.response.PageData;
import com.cyclosa.employee.service.EmployeeService;
import com.cyclosa.workplace.dto.filter.WorkplaceSeatFilter;
import com.cyclosa.workplace.dto.request.AssignSeatRequest;
import com.cyclosa.workplace.dto.request.BulkGenerateSeatsRequest;
import com.cyclosa.workplace.dto.request.ChangeSeatStatusRequest;
import com.cyclosa.workplace.dto.request.CreateSeatRequest;
import com.cyclosa.workplace.dto.request.UpdateSeatRequest;
import com.cyclosa.workplace.dto.response.WorkplaceSeatDetailResponse;
import com.cyclosa.workplace.dto.response.WorkplaceSeatResponse;
import com.cyclosa.workplace.entity.*;
import com.cyclosa.workplace.enums.ClusterType;
import com.cyclosa.workplace.enums.SeatStatus;
import com.cyclosa.workplace.enums.SeatType;
import com.cyclosa.workplace.exception.WorkplaceErrorCode;
import com.cyclosa.workplace.mapper.WorkplaceMapper;
import com.cyclosa.workplace.repository.WorkplaceRowRepository;
import com.cyclosa.workplace.repository.WorkplaceSeatLinkedAssetRepository;
import com.cyclosa.workplace.repository.WorkplaceSeatRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkplaceSeatService {

    private final WorkplaceSeatRepository seatRepository;
    private final WorkplaceRowRepository rowRepository;
    private final WorkplaceSeatLinkedAssetRepository linkedAssetRepository;
    private final WorkplaceMapper workplaceMapper;
    private final EmployeeService employeeService;
    private final AssetService assetService;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Transactional
    public WorkplaceSeatResponse createSeat(UUID companyId, CreateSeatRequest request) {
        String seatCode = request.getSeatCode().trim().toUpperCase();
        if (seatRepository.existsByCompanyIdAndSeatCode(companyId, seatCode)) {
            throw new AppException(WorkplaceErrorCode.SEAT_CODE_ALREADY_EXISTS);
        }

        WorkplaceRow row = rowRepository.findByIdAndCompanyIdWithHierarchy(request.getRowId(), companyId)
                .orElseThrow(() -> new AppException(WorkplaceErrorCode.ROW_NOT_FOUND));

        if (request.getAssignedEmployeeId() != null) {
            validateEmployeeAssignment(companyId, request.getAssignedEmployeeId(), null);
        }

        WorkplaceSeat seat = WorkplaceSeat.builder()
                .companyId(companyId)
                .row(row)
                .seatCode(seatCode)
                .seatType(request.getSeatType())
                .status(request.getAssignedEmployeeId() != null ? SeatStatus.OCCUPIED : request.getStatus())
                .deskShape(request.getDeskShape())
                .positionX(request.getPositionX())
                .positionY(request.getPositionY())
                .width(request.getWidth() != null ? request.getWidth() : BigDecimal.valueOf(60))
                .height(request.getHeight() != null ? request.getHeight() : BigDecimal.valueOf(40))
                .rotation(request.getRotation() != null ? request.getRotation() : BigDecimal.ZERO)
                .amenities(toJsonString(request.getAmenities()))
                .assignedEmployeeId(request.getAssignedEmployeeId())
                .qrCodeData(generateQrCodeData(companyId, seatCode))
                .visualMetadata(request.getVisualMetadata())
                .build();

        seat = seatRepository.save(seat);
        log.info("Created workplace seat id={}, code={}, companyId={}", seat.getId(), seat.getSeatCode(), companyId);

        WorkplaceSeatResponse response = workplaceMapper.toSeatResponse(seat);
        response.setAmenities(parseJsonList(seat.getAmenities()));
        enrichEmployees(List.of(response));
        return response;
    }

    @Transactional
    public WorkplaceSeatResponse updateSeat(UUID companyId, UUID seatId, UpdateSeatRequest request) {
        WorkplaceSeat seat = seatRepository.findByIdAndCompanyIdWithHierarchy(seatId, companyId)
                .orElseThrow(() -> new AppException(WorkplaceErrorCode.SEAT_NOT_FOUND));

        String newCode = request.getSeatCode().trim().toUpperCase();
        if (seatRepository.existsByCompanyIdAndSeatCodeAndIdNot(companyId, newCode, seatId)) {
            throw new AppException(WorkplaceErrorCode.SEAT_CODE_ALREADY_EXISTS);
        }

        if (request.getAssignedEmployeeId() != null && !request.getAssignedEmployeeId().equals(seat.getAssignedEmployeeId())) {
            validateEmployeeAssignment(companyId, request.getAssignedEmployeeId(), seatId);
        }

        seat.setSeatCode(newCode);
        seat.setSeatType(request.getSeatType());
        seat.setStatus(request.getStatus());
        seat.setDeskShape(request.getDeskShape());
        seat.setPositionX(request.getPositionX());
        seat.setPositionY(request.getPositionY());
        if (request.getWidth() != null) seat.setWidth(request.getWidth());
        if (request.getHeight() != null) seat.setHeight(request.getHeight());
        if (request.getRotation() != null) seat.setRotation(request.getRotation());
        seat.setAmenities(toJsonString(request.getAmenities()));
        seat.setAssignedEmployeeId(request.getAssignedEmployeeId());
        seat.setVisualMetadata(request.getVisualMetadata());

        if (seat.getAssignedEmployeeId() != null && seat.getStatus() == SeatStatus.AVAILABLE) {
            seat.setStatus(SeatStatus.OCCUPIED);
        }

        seat = seatRepository.save(seat);
        log.info("Updated workplace seat id={}, code={}", seat.getId(), seat.getSeatCode());

        WorkplaceSeatResponse response = workplaceMapper.toSeatResponse(seat);
        response.setAmenities(parseJsonList(seat.getAmenities()));
        enrichEmployees(List.of(response));
        return response;
    }

    @Transactional(readOnly = true)
    public WorkplaceSeatDetailResponse getSeatDetail(UUID companyId, UUID seatId) {
        WorkplaceSeat seat = seatRepository.findByIdAndCompanyIdWithHierarchy(seatId, companyId)
                .orElseThrow(() -> new AppException(WorkplaceErrorCode.SEAT_NOT_FOUND));

        WorkplaceRow row = seat.getRow();
        WorkplaceZone zone = row.getZone();
        WorkplaceFloor floor = zone.getFloor();
        WorkplaceSite site = floor.getSite();

        List<WorkplaceSeatLinkedAsset> linkedAssets = linkedAssetRepository.findByCompanyIdAndSeatId(companyId, seatId);
        List<AssetDetailResponse> assetDetails = new ArrayList<>();
        for (WorkplaceSeatLinkedAsset la : linkedAssets) {
            try {
                AssetDetailResponse detail = assetService.getAssetById(la.getAssetId());
                if (detail != null) {
                    assetDetails.add(detail);
                }
            } catch (Exception e) {
                log.warn("Cannot fetch asset detail for assetId={}: {}", la.getAssetId(), e.getMessage());
            }
        }

        EmployeeSummary empSummary = null;
        if (seat.getAssignedEmployeeId() != null) {
            Map<UUID, EmployeeSummary> empMap = employeeService.getEmployeeSummaries(Set.of(seat.getAssignedEmployeeId()));
            empSummary = empMap.get(seat.getAssignedEmployeeId());
        }

        return WorkplaceSeatDetailResponse.builder()
                .id(seat.getId())
                .companyId(companyId)
                .siteId(site.getId())
                .siteName(site.getName())
                .siteCode(site.getCode())
                .floorId(floor.getId())
                .floorNumber(floor.getFloorNumber())
                .zoneId(zone.getId())
                .zoneName(zone.getName())
                .rowId(row.getId())
                .rowName(row.getName())
                .seatCode(seat.getSeatCode())
                .seatType(seat.getSeatType())
                .status(seat.getStatus())
                .deskShape(seat.getDeskShape())
                .positionX(seat.getPositionX())
                .positionY(seat.getPositionY())
                .width(seat.getWidth())
                .height(seat.getHeight())
                .rotation(seat.getRotation())
                .amenities(parseJsonList(seat.getAmenities()))
                .assignedEmployeeId(seat.getAssignedEmployeeId())
                .assignedEmployee(empSummary)
                .qrCodeData(seat.getQrCodeData())
                .visualMetadata(seat.getVisualMetadata())
                .linkedAssets(assetDetails)
                .createdAt(seat.getCreatedAt())
                .updatedAt(seat.getUpdatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public PageData<WorkplaceSeatResponse> filterSeats(UUID companyId, WorkplaceSeatFilter filter, Pageable pageable) {
        Page<WorkplaceSeat> page = seatRepository.filterSeats(companyId,
                filter.getFloorId(),
                filter.getZoneId(),
                filter.getRowId(),
                filter.getSeatType(),
                filter.getStatus(),
                filter.getKeyword(),
                pageable);

        if (page.isEmpty()) {
            return PageData.empty(pageable);
        }

        Set<UUID> seatIds = page.getContent().stream().map(WorkplaceSeat::getId).collect(Collectors.toSet());
        List<WorkplaceSeatLinkedAsset> allLinkedAssets = linkedAssetRepository.findBySeatIdInAndCompanyId(seatIds, companyId);
        Map<UUID, Long> linkedCountMap = allLinkedAssets.stream()
                .collect(Collectors.groupingBy(la -> la.getSeat().getId(), Collectors.counting()));

        List<WorkplaceSeatResponse> list = page.getContent().stream().map(seat -> {
            WorkplaceSeatResponse res = workplaceMapper.toSeatResponse(seat);
            res.setAmenities(parseJsonList(seat.getAmenities()));
            res.setLinkedAssetCount(linkedCountMap.getOrDefault(seat.getId(), 0L).intValue());
            return res;
        }).toList();

        enrichEmployees(list);
        return PageData.of(page, list);
    }

    @Transactional
    public List<WorkplaceSeatResponse> bulkGenerateSeats(UUID companyId, UUID rowId, BulkGenerateSeatsRequest request) {
        WorkplaceRow row = rowRepository.findByIdAndCompanyIdWithHierarchy(rowId, companyId)
                .orElseThrow(() -> new AppException(WorkplaceErrorCode.ROW_NOT_FOUND));

        WorkplaceZone zone = row.getZone();
        WorkplaceFloor floor = zone.getFloor();
        WorkplaceSite site = floor.getSite();

        String amenitiesJson = toJsonString(request.getAmenities());
        List<WorkplaceSeat> toSave = new ArrayList<>();

        double ox = request.getOriginX().doubleValue();
        double oy = request.getOriginY().doubleValue();
        double dw = request.getDeskWidth().doubleValue();
        double dh = request.getDeskHeight().doubleValue();
        double sx = request.getSpacingX().doubleValue();
        double sy = request.getSpacingY().doubleValue();

        ClusterType clusterType = request.getClusterType() != null ? request.getClusterType() : ClusterType.SINGLE_ROW;

        for (int i = 0; i < request.getCount(); i++) {
            int seatNum = request.getStartNumber() + i;
            String seatNumberStr = String.format("%02d", seatNum);
            String codePrefix = request.getCodePrefix() != null ? request.getCodePrefix().trim() : "S";
            String fullCode = String.format("%s-%s-%s-%s-%s%s",
                    site.getCode(), floor.getFloorNumber(), zone.getName(), row.getName(), codePrefix, seatNumberStr);

            if (seatRepository.existsByCompanyIdAndSeatCode(companyId, fullCode)) {
                log.warn("Skipping existing seat code: {}", fullCode);
                continue;
            }

            double posX = ox;
            double posY = oy;
            double rot = request.getRotation() != null ? request.getRotation().doubleValue() : 0.0;

            if (clusterType == ClusterType.SINGLE_ROW) {
                // Thẳng hàng theo trục X
                posX = ox + i * (dw + sx);
                posY = oy;
            } else if (clusterType == ClusterType.POD_4_FACING) {
                // 2x2 đối diện: hàng trên i=0,1 (hướng xuống), hàng dưới i=2,3 (hướng lên)
                int col = i % 2;
                int rIndex = i / 2;
                posX = ox + col * (dw + sx);
                posY = oy + rIndex * (dh + sy);
                rot = (rIndex == 0) ? 180.0 : 0.0;
            } else if (clusterType == ClusterType.POD_6_FACING) {
                // 3x2 đối diện: hàng trên 3 ghế, hàng dưới 3 ghế
                int col = i % 3;
                int rIndex = i / 3;
                posX = ox + col * (dw + sx);
                posY = oy + rIndex * (dh + sy);
                rot = (rIndex == 0) ? 180.0 : 0.0;
            }

            WorkplaceSeat seat = WorkplaceSeat.builder()
                    .companyId(companyId)
                    .row(row)
                    .seatCode(fullCode)
                    .seatType(request.getSeatType())
                    .status(SeatStatus.AVAILABLE)
                    .deskShape(request.getDeskShape())
                    .positionX(BigDecimal.valueOf(posX).setScale(2, RoundingMode.HALF_UP))
                    .positionY(BigDecimal.valueOf(posY).setScale(2, RoundingMode.HALF_UP))
                    .width(request.getDeskWidth())
                    .height(request.getDeskHeight())
                    .rotation(BigDecimal.valueOf(rot).setScale(2, RoundingMode.HALF_UP))
                    .amenities(amenitiesJson)
                    .qrCodeData(generateQrCodeData(companyId, fullCode))
                    .build();

            toSave.add(seat);
        }

        if (!toSave.isEmpty()) {
            toSave = seatRepository.saveAll(toSave);
        }

        List<WorkplaceSeatResponse> responses = workplaceMapper.toSeatResponseList(toSave);
        responses.forEach(r -> r.setAmenities(request.getAmenities()));
        return responses;
    }

    @Transactional
    public WorkplaceSeatResponse assignSeat(UUID companyId, UUID seatId, AssignSeatRequest request) {
        WorkplaceSeat seat = seatRepository.findByIdAndCompanyIdWithHierarchy(seatId, companyId)
                .orElseThrow(() -> new AppException(WorkplaceErrorCode.SEAT_NOT_FOUND));

        if (seat.getStatus() == SeatStatus.MAINTENANCE || seat.getStatus() == SeatStatus.BLOCKED) {
            throw new AppException(WorkplaceErrorCode.SEAT_NOT_AVAILABLE);
        }

        validateEmployeeAssignment(companyId, request.getEmployeeId(), seatId);

        seat.setAssignedEmployeeId(request.getEmployeeId());
        seat.setSeatType(SeatType.DEDICATED);
        seat.setStatus(SeatStatus.OCCUPIED);

        seat = seatRepository.save(seat);
        log.info("Assigned seat id={}, code={} to employee id={}", seat.getId(), seat.getSeatCode(), request.getEmployeeId());

        WorkplaceSeatResponse response = workplaceMapper.toSeatResponse(seat);
        response.setAmenities(parseJsonList(seat.getAmenities()));
        enrichEmployees(List.of(response));
        return response;
    }

    @Transactional
    public WorkplaceSeatResponse releaseSeat(UUID companyId, UUID seatId) {
        WorkplaceSeat seat = seatRepository.findByIdAndCompanyIdWithHierarchy(seatId, companyId)
                .orElseThrow(() -> new AppException(WorkplaceErrorCode.SEAT_NOT_FOUND));

        if (seat.getAssignedEmployeeId() == null) {
            throw new AppException(WorkplaceErrorCode.SEAT_NOT_OCCUPIED);
        }

        log.info("Releasing seat id={}, code={} from employee id={}", seat.getId(), seat.getSeatCode(), seat.getAssignedEmployeeId());
        seat.setAssignedEmployeeId(null);
        seat.setStatus(SeatStatus.AVAILABLE);

        seat = seatRepository.save(seat);

        WorkplaceSeatResponse response = workplaceMapper.toSeatResponse(seat);
        response.setAmenities(parseJsonList(seat.getAmenities()));
        return response;
    }

    @Transactional
    public WorkplaceSeatResponse changeStatus(UUID companyId, UUID seatId, ChangeSeatStatusRequest request) {
        WorkplaceSeat seat = seatRepository.findByIdAndCompanyIdWithHierarchy(seatId, companyId)
                .orElseThrow(() -> new AppException(WorkplaceErrorCode.SEAT_NOT_FOUND));

        seat.setStatus(request.getStatus());
        if (request.getStatus() == SeatStatus.AVAILABLE) {
            seat.setAssignedEmployeeId(null);
        }

        seat = seatRepository.save(seat);
        log.info("Changed status of seat id={} to {}", seat.getId(), request.getStatus());

        WorkplaceSeatResponse response = workplaceMapper.toSeatResponse(seat);
        response.setAmenities(parseJsonList(seat.getAmenities()));
        enrichEmployees(List.of(response));
        return response;
    }

    // --- Asset Linking (Module 13 Integration) ---

    @Transactional
    public void linkAsset(UUID companyId, UUID seatId, UUID assetId) {
        WorkplaceSeat seat = seatRepository.findByIdAndCompanyId(seatId, companyId)
                .orElseThrow(() -> new AppException(WorkplaceErrorCode.SEAT_NOT_FOUND));

        // Kiểm tra tài sản có tồn tại qua Module 13 Public Service
        assetService.getAssetById(assetId);

        if (linkedAssetRepository.existsByCompanyIdAndSeatIdAndAssetId(companyId, seatId, assetId)) {
            throw new AppException(WorkplaceErrorCode.ASSET_ALREADY_LINKED);
        }

        WorkplaceSeatLinkedAsset link = WorkplaceSeatLinkedAsset.builder()
                .companyId(companyId)
                .seat(seat)
                .assetId(assetId)
                .build();

        linkedAssetRepository.save(link);
        log.info("Linked asset id={} to seat id={}", assetId, seatId);
    }

    @Transactional
    public void unlinkAsset(UUID companyId, UUID seatId, UUID assetId) {
        if (!linkedAssetRepository.existsByCompanyIdAndSeatIdAndAssetId(companyId, seatId, assetId)) {
            throw new AppException(WorkplaceErrorCode.ASSET_LINK_NOT_FOUND);
        }
        linkedAssetRepository.deleteByCompanyIdAndSeatIdAndAssetId(companyId, seatId, assetId);
        log.info("Unlinked asset id={} from seat id={}", assetId, seatId);
    }

    // --- Lifecycle Hooks (Onboarding & Offboarding) ---

    @Transactional(readOnly = true)
    public List<WorkplaceSeatResponse> recommendSeatsForOnboarding(UUID companyId, UUID departmentId, UUID floorId) {
        List<WorkplaceSeat> seats = seatRepository.findRecommendedAvailableSeats(companyId, departmentId, floorId);
        List<WorkplaceSeatResponse> responses = workplaceMapper.toSeatResponseList(seats);
        responses.forEach(r -> r.setAmenities(parseJsonList(r.getAmenities() != null ? r.getAmenities().toString() : null)));
        return responses;
    }

    @Transactional
    public void releaseSeatByEmployeeId(UUID companyId, UUID employeeId) {
        Optional<WorkplaceSeat> optSeat = seatRepository.findByCompanyIdAndAssignedEmployeeId(companyId, employeeId);
        if (optSeat.isPresent()) {
            WorkplaceSeat seat = optSeat.get();
            log.info("[Offboarding Hook] Releasing seat id={}, code={} for offboarding employee id={}",
                    seat.getId(), seat.getSeatCode(), employeeId);
            seat.setAssignedEmployeeId(null);
            seat.setStatus(SeatStatus.AVAILABLE);
            seatRepository.save(seat);
        }
    }

    @Transactional
    public void deleteSeat(UUID companyId, UUID seatId) {
        WorkplaceSeat seat = seatRepository.findByIdAndCompanyId(seatId, companyId)
                .orElseThrow(() -> new AppException(WorkplaceErrorCode.SEAT_NOT_FOUND));

        linkedAssetRepository.deleteByCompanyIdAndSeatId(companyId, seatId);
        seatRepository.delete(seat);
        log.info("Deleted seat id={}", seatId);
    }

    // --- Private Helper Methods ---

    private void validateEmployeeAssignment(UUID companyId, UUID employeeId, UUID currentSeatId) {
        Optional<WorkplaceSeat> existingSeat = seatRepository.findByCompanyIdAndAssignedEmployeeId(companyId, employeeId);
        if (existingSeat.isPresent() && (currentSeatId == null || !existingSeat.get().getId().equals(currentSeatId))) {
            throw new AppException(WorkplaceErrorCode.EMPLOYEE_ALREADY_HAS_SEAT);
        }
    }

    private String generateQrCodeData(UUID companyId, String seatCode) {
        return "CYCLOSA:WORKPLACE:SEAT:" + companyId + ":" + seatCode;
    }

    private String toJsonString(List<String> list) {
        if (list == null || list.isEmpty()) return null;
        try {
            return objectMapper.writeValueAsString(list);
        } catch (Exception e) {
            return null;
        }
    }

    private List<String> parseJsonList(String json) {
        if (json == null || json.isBlank()) return Collections.emptyList();
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private void enrichEmployees(List<WorkplaceSeatResponse> responses) {
        if (responses == null || responses.isEmpty()) return;
        Set<UUID> empIds = responses.stream()
                .map(WorkplaceSeatResponse::getAssignedEmployeeId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (empIds.isEmpty()) return;

        Map<UUID, EmployeeSummary> empMap = employeeService.getEmployeeSummaries(empIds);
        responses.forEach(r -> {
            if (r.getAssignedEmployeeId() != null) {
                r.setAssignedEmployee(empMap.get(r.getAssignedEmployeeId()));
            }
        });
    }

    @Transactional(readOnly = true)
    public List<WorkplaceSeatResponse> getSeatsByFloor(UUID floorId) {
        List<WorkplaceSeat> seats = seatRepository.findByFloorIdWithHierarchy(floorId);
        List<WorkplaceSeatResponse> responses = workplaceMapper.toSeatResponseList(seats);
        responses.forEach(r -> r.setAmenities(parseJsonList(r.getAmenities() != null ? r.getAmenities().toString() : null)));
        enrichEmployees(responses);
        return responses;
    }

    @Transactional(readOnly = true)
    public WorkplaceSeatDetailResponse getSeatDetail(UUID seatId) {
        WorkplaceSeat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new AppException(WorkplaceErrorCode.SEAT_NOT_FOUND));
        return getSeatDetail(seat.getCompanyId(), seatId);
    }
}
