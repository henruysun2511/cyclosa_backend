package com.cyclosa.workplace.service;

import com.cyclosa.common.exception.AppException;
import com.cyclosa.workplace.dto.request.CreateFloorPlanRequest;
import com.cyclosa.workplace.dto.request.CreateNonDeskObjectRequest;
import com.cyclosa.workplace.dto.request.UpdateNonDeskObjectRequest;
import com.cyclosa.workplace.dto.response.*;
import com.cyclosa.workplace.entity.*;
import com.cyclosa.workplace.enums.NonDeskObjectType;
import com.cyclosa.workplace.exception.WorkplaceErrorCode;
import com.cyclosa.workplace.mapper.WorkplaceMapper;
import com.cyclosa.workplace.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkplaceFloorPlanService {

    private final WorkplaceFloorPlanRepository floorPlanRepository;
    private final WorkplaceFloorRepository floorRepository;
    private final WorkplaceZoneRepository zoneRepository;
    private final WorkplaceRowRepository rowRepository;
    private final WorkplaceNonDeskObjectRepository nonDeskObjectRepository;
    private final WorkplaceSeatService seatService;
    private final WorkplaceMapper workplaceMapper;
    private final WorkplaceSecurityContext securityContext;

    // =========================================================================
    // FLOOR PLAN MANAGEMENT
    // =========================================================================

    @Transactional(readOnly = true)
    public WorkplaceFloorPlanResponse getFloorPlanByFloorId(UUID floorId) {
        WorkplaceFloorPlan floorPlan = floorPlanRepository.findByFloorIdAndIsPublishedTrue(floorId)
                .orElseGet(() -> floorPlanRepository.findByFloorId(floorId)
                        .orElseThrow(() -> new AppException(WorkplaceErrorCode.FLOOR_PLAN_NOT_FOUND)));
        return workplaceMapper.toFloorPlanResponse(floorPlan);
    }

    @Transactional
    public WorkplaceFloorPlanResponse saveOrUpdateFloorPlan(UUID floorId, CreateFloorPlanRequest request) {
        WorkplaceFloor floor = floorRepository.findById(floorId)
                .orElseThrow(() -> new AppException(WorkplaceErrorCode.FLOOR_NOT_FOUND));

        UUID companyId = floor.getSite().getCompanyId();
        if (companyId == null) {
            companyId = securityContext.getCurrentCompanyId();
        }

        WorkplaceFloorPlan floorPlan = floorPlanRepository.findByFloorId(floorId)
                .orElse(null);

        if (floorPlan == null) {
            floorPlan = WorkplaceFloorPlan.builder()
                    .companyId(companyId)
                    .floor(floor)
                    .backgroundImageUrl(request.getBackgroundImageUrl())
                    .canvasWidth(request.getCanvasWidth() != null ? request.getCanvasWidth() : BigDecimal.valueOf(3000))
                    .canvasHeight(request.getCanvasHeight() != null ? request.getCanvasHeight() : BigDecimal.valueOf(2000))
                    .scaleRatio(request.getScaleRatio() != null ? request.getScaleRatio() : BigDecimal.valueOf(0.05))
                    .version(1)
                    .isPublished(request.getIsPublished() != null ? request.getIsPublished() : true)
                    .build();
        } else {
            floorPlan.setBackgroundImageUrl(request.getBackgroundImageUrl());
            if (request.getCanvasWidth() != null) floorPlan.setCanvasWidth(request.getCanvasWidth());
            if (request.getCanvasHeight() != null) floorPlan.setCanvasHeight(request.getCanvasHeight());
            if (request.getScaleRatio() != null) floorPlan.setScaleRatio(request.getScaleRatio());
            if (request.getIsPublished() != null) floorPlan.setIsPublished(request.getIsPublished());
            floorPlan.setVersion(floorPlan.getVersion() + 1);
        }

        floorPlan = floorPlanRepository.save(floorPlan);
        log.info("Saved floor plan for floor {}: version {}", floorId, floorPlan.getVersion());
        return workplaceMapper.toFloorPlanResponse(floorPlan);
    }

    @Transactional(readOnly = true)
    public FloorLayoutCanvasResponse getFloorLayoutCanvas(UUID floorId) {
        WorkplaceFloor floor = floorRepository.findById(floorId)
                .orElseThrow(() -> new AppException(WorkplaceErrorCode.FLOOR_NOT_FOUND));

        WorkplaceFloorPlan floorPlan = floorPlanRepository.findByFloorId(floorId).orElse(null);

        // Lấy danh sách Zone thuộc Floor
        List<WorkplaceZone> zones = zoneRepository.findByFloorIdOrderByZoneCodeAsc(floorId);
        List<WorkplaceZoneResponse> zoneResponses = zones.stream()
                .map(workplaceMapper::toZoneResponse)
                .collect(Collectors.toList());

        // Lấy danh sách Row thuộc các Zone
        List<UUID> zoneIds = zones.stream().map(WorkplaceZone::getId).collect(Collectors.toList());
        List<WorkplaceRowResponse> rowResponses = new ArrayList<>();
        if (!zoneIds.isEmpty()) {
            List<WorkplaceRow> rows = rowRepository.findByZoneIdInOrderByRowCodeAsc(zoneIds);
            rowResponses = rows.stream()
                    .map(workplaceMapper::toRowResponse)
                    .collect(Collectors.toList());
        }

        // Lấy danh sách Seats kèm thông tin nhân sự tóm tắt (Nested Summary)
        List<WorkplaceSeatResponse> seatResponses = seatService.getSeatsByFloor(floorId);

        // Lấy danh sách vật thể phi bàn làm việc (phòng họp, thang máy, WC...)
        List<WorkplaceNonDeskObject> nonDeskObjects = nonDeskObjectRepository.findByFloorId(floorId);
        List<WorkplaceNonDeskObjectResponse> nonDeskObjectResponses = nonDeskObjects.stream()
                .map(workplaceMapper::toNonDeskObjectResponse)
                .collect(Collectors.toList());

        return FloorLayoutCanvasResponse.builder()
                .floorId(floor.getId())
                .floorNumber(floor.getFloorNumber())
                .floorName(floor.getName())
                .siteId(floor.getSite().getId())
                .siteName(floor.getSite().getName())
                .backgroundImageUrl(floorPlan != null ? floorPlan.getBackgroundImageUrl() : null)
                .canvasWidth(floorPlan != null ? floorPlan.getCanvasWidth() : BigDecimal.valueOf(3000))
                .canvasHeight(floorPlan != null ? floorPlan.getCanvasHeight() : BigDecimal.valueOf(2000))
                .scaleRatio(floorPlan != null ? floorPlan.getScaleRatio() : BigDecimal.valueOf(0.05))
                .floorPlanVersion(floorPlan != null ? floorPlan.getVersion() : 1)
                .zones(zoneResponses)
                .rows(rowResponses)
                .seats(seatResponses)
                .nonDeskObjects(nonDeskObjectResponses)
                .build();
    }

    // =========================================================================
    // NON-DESK OBJECT MANAGEMENT (Meeting rooms, elevators, stairs, restrooms)
    // =========================================================================

    @Transactional
    public WorkplaceNonDeskObjectResponse createNonDeskObject(CreateNonDeskObjectRequest request) {
        WorkplaceZone zone = zoneRepository.findById(request.getZoneId())
                .orElseThrow(() -> new AppException(WorkplaceErrorCode.ZONE_NOT_FOUND));

        UUID companyId = zone.getFloor().getSite().getCompanyId();
        if (companyId == null) {
            companyId = securityContext.getCurrentCompanyId();
        }

        WorkplaceNonDeskObject object = WorkplaceNonDeskObject.builder()
                .companyId(companyId)
                .zone(zone)
                .name(request.getName().trim())
                .objectType(request.getObjectType())
                .positionX(request.getPositionX())
                .positionY(request.getPositionY())
                .width(request.getWidth() != null ? request.getWidth() : BigDecimal.valueOf(100))
                .height(request.getHeight() != null ? request.getHeight() : BigDecimal.valueOf(100))
                .rotation(request.getRotation() != null ? request.getRotation() : BigDecimal.ZERO)
                .meetingRoomCapacity(request.getMeetingRoomCapacity())
                .description(request.getDescription())
                .qrCodeData("CYCLOSA:WORKPLACE:OBJECT:" + companyId + ":" + request.getName().trim())
                .build();

        object = nonDeskObjectRepository.save(object);
        log.info("Created non-desk object {} ({}) in zone {}", object.getName(), object.getObjectType(), zone.getId());
        return workplaceMapper.toNonDeskObjectResponse(object);
    }

    @Transactional
    public WorkplaceNonDeskObjectResponse updateNonDeskObject(UUID id, UpdateNonDeskObjectRequest request) {
        WorkplaceNonDeskObject object = nonDeskObjectRepository.findById(id)
                .orElseThrow(() -> new AppException(WorkplaceErrorCode.NON_DESK_OBJECT_NOT_FOUND));

        if (request.getName() != null) object.setName(request.getName().trim());
        if (request.getObjectType() != null) object.setObjectType(request.getObjectType());
        if (request.getPositionX() != null) object.setPositionX(request.getPositionX());
        if (request.getPositionY() != null) object.setPositionY(request.getPositionY());
        if (request.getWidth() != null) object.setWidth(request.getWidth());
        if (request.getHeight() != null) object.setHeight(request.getHeight());
        if (request.getRotation() != null) object.setRotation(request.getRotation());
        if (request.getMeetingRoomCapacity() != null) object.setMeetingRoomCapacity(request.getMeetingRoomCapacity());
        if (request.getDescription() != null) object.setDescription(request.getDescription());

        object = nonDeskObjectRepository.save(object);
        return workplaceMapper.toNonDeskObjectResponse(object);
    }

    @Transactional
    public void deleteNonDeskObject(UUID id) {
        WorkplaceNonDeskObject object = nonDeskObjectRepository.findById(id)
                .orElseThrow(() -> new AppException(WorkplaceErrorCode.NON_DESK_OBJECT_NOT_FOUND));
        nonDeskObjectRepository.delete(object);
        log.info("Deleted non-desk object {}", id);
    }

    @Transactional(readOnly = true)
    public WorkplaceNonDeskObjectResponse getNonDeskObjectById(UUID id) {
        WorkplaceNonDeskObject object = nonDeskObjectRepository.findById(id)
                .orElseThrow(() -> new AppException(WorkplaceErrorCode.NON_DESK_OBJECT_NOT_FOUND));
        return workplaceMapper.toNonDeskObjectResponse(object);
    }

    @Transactional(readOnly = true)
    public List<WorkplaceNonDeskObjectResponse> getNonDeskObjectsByFloor(UUID floorId) {
        return nonDeskObjectRepository.findByFloorId(floorId).stream()
                .map(workplaceMapper::toNonDeskObjectResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<WorkplaceNonDeskObjectResponse> getBookableRooms(UUID floorId) {
        List<WorkplaceNonDeskObject> rooms;
        if (floorId != null) {
            rooms = nonDeskObjectRepository.findByFloorIdAndObjectType(floorId, NonDeskObjectType.MEETING_ROOM);
        } else {
            rooms = nonDeskObjectRepository.findByObjectType(NonDeskObjectType.MEETING_ROOM);
        }
        return rooms.stream()
                .map(workplaceMapper::toNonDeskObjectResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public WorkplaceNonDeskObject getNonDeskObjectEntity(UUID id) {
        return nonDeskObjectRepository.findById(id)
                .orElseThrow(() -> new AppException(WorkplaceErrorCode.NON_DESK_OBJECT_NOT_FOUND));
    }
}
