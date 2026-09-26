package com.cyclosa.workplace.service;

import com.cyclosa.common.exception.AppException;
import com.cyclosa.workplace.dto.request.NavigationRequest;
import com.cyclosa.workplace.dto.response.NavigationRouteResponse;
import com.cyclosa.workplace.dto.response.NavigationRouteResponse.Point2D;
import com.cyclosa.workplace.dto.response.NavigationRouteResponse.WaypointLocation;
import com.cyclosa.workplace.entity.*;
import com.cyclosa.workplace.enums.NavigationType;
import com.cyclosa.workplace.enums.NonDeskObjectType;
import com.cyclosa.workplace.exception.WorkplaceErrorCode;
import com.cyclosa.workplace.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkplaceNavigationService {

    private final WorkplaceSeatRepository seatRepository;
    private final WorkplaceNonDeskObjectRepository nonDeskObjectRepository;
    private final WorkplaceFloorPlanRepository floorPlanRepository;
    private final AStarPathfinder pathfinder;
    private final WorkplaceSecurityContext securityContext;

    @Transactional(readOnly = true)
    public NavigationRouteResponse calculateRoute(NavigationRequest request) {
        UUID companyId = securityContext.getCurrentCompanyId();

        // 1. Phân giải điểm xuất phát (Origin)
        ResolvedWaypoint origin = resolveOrigin(companyId, request);
        // 2. Phân giải điểm đến (Destination)
        ResolvedWaypoint destination = resolveDestination(companyId, request);

        // Kịch bản 1: Khác cơ sở (CROSS_SITE)
        if (!origin.siteId.equals(destination.siteId)) {
            return buildCrossSiteResponse(origin, destination);
        }

        // Kịch bản 2: Cùng cơ sở nhưng khác tầng (CROSS_FLOOR)
        if (!origin.floorId.equals(destination.floorId)) {
            return buildCrossFloorResponse(companyId, origin, destination);
        }

        // Kịch bản 3: Cùng một tầng (SAME_FLOOR) -> Dẫn đường chi tiết bằng thuật toán A*
        return buildSameFloorResponse(companyId, origin, destination);
    }

    // =========================================================================
    // RESOLUTION HELPERS
    // =========================================================================

    private static class ResolvedWaypoint {
        UUID siteId;
        String siteName;
        UUID floorId;
        String floorNumber;
        UUID zoneId;
        String zoneName;
        UUID rowId;
        String rowName;
        UUID seatId;
        String seatCode;
        UUID objectId;
        String objectName;
        String title;
        BigDecimal x;
        BigDecimal y;
    }

    private ResolvedWaypoint resolveOrigin(UUID companyId, NavigationRequest request) {
        if (request.getOriginSeatId() != null) {
            return resolveFromSeatId(companyId, request.getOriginSeatId());
        }
        if (request.getOriginObjectId() != null) {
            return resolveFromObjectId(request.getOriginObjectId());
        }
        if (request.getOriginEmployeeId() != null) {
            return resolveFromEmployeeId(companyId, request.getOriginEmployeeId());
        }
        // Fallback: nếu user hiện tại đã đăng nhập và có ghế ngồi
        UUID currentEmployeeId = securityContext.getRequiredEmployeeId();
        if (currentEmployeeId != null) {
            try {
                return resolveFromEmployeeId(companyId, currentEmployeeId);
            } catch (Exception ignored) {
            }
        }
        throw new AppException(WorkplaceErrorCode.NAVIGATION_ORIGIN_NOT_FOUND);
    }

    private ResolvedWaypoint resolveDestination(UUID companyId, NavigationRequest request) {
        if (request.getDestinationSeatId() != null) {
            return resolveFromSeatId(companyId, request.getDestinationSeatId());
        }
        if (request.getDestinationObjectId() != null) {
            return resolveFromObjectId(request.getDestinationObjectId());
        }
        if (request.getDestinationEmployeeId() != null) {
            return resolveFromEmployeeId(companyId, request.getDestinationEmployeeId());
        }
        throw new AppException(WorkplaceErrorCode.NAVIGATION_DESTINATION_NOT_FOUND);
    }

    private ResolvedWaypoint resolveFromSeatId(UUID companyId, UUID seatId) {
        WorkplaceSeat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new AppException(WorkplaceErrorCode.SEAT_NOT_FOUND));

        if (companyId != null && seat.getCompanyId() != null && !companyId.equals(seat.getCompanyId())) {
            throw new AppException(WorkplaceErrorCode.SEAT_NOT_FOUND);
        }

        ResolvedWaypoint wp = new ResolvedWaypoint();
        wp.siteId = seat.getRow().getZone().getFloor().getSite().getId();
        wp.siteName = seat.getRow().getZone().getFloor().getSite().getName();
        wp.floorId = seat.getRow().getZone().getFloor().getId();
        wp.floorNumber = seat.getRow().getZone().getFloor().getFloorNumber();
        wp.zoneId = seat.getRow().getZone().getId();
        wp.zoneName = seat.getRow().getZone().getName();
        wp.rowId = seat.getRow().getId();
        wp.rowName = seat.getRow().getName();
        wp.seatId = seat.getId();
        wp.seatCode = seat.getSeatCode();
        wp.title = "Chỗ ngồi " + seat.getSeatCode();
        wp.x = seat.getPositionX();
        wp.y = seat.getPositionY();
        return wp;
    }

    private ResolvedWaypoint resolveFromObjectId(UUID objectId) {
        WorkplaceNonDeskObject obj = nonDeskObjectRepository.findById(objectId)
                .orElseThrow(() -> new AppException(WorkplaceErrorCode.NON_DESK_OBJECT_NOT_FOUND));

        ResolvedWaypoint wp = new ResolvedWaypoint();
        wp.siteId = obj.getZone().getFloor().getSite().getId();
        wp.siteName = obj.getZone().getFloor().getSite().getName();
        wp.floorId = obj.getZone().getFloor().getId();
        wp.floorNumber = obj.getZone().getFloor().getFloorNumber();
        if (obj.getZone() != null) {
            wp.zoneId = obj.getZone().getId();
            wp.zoneName = obj.getZone().getName();
        }
        wp.objectId = obj.getId();
        wp.objectName = obj.getName();
        wp.title = obj.getName() + " (" + obj.getObjectType() + ")";
        wp.x = obj.getPositionX();
        wp.y = obj.getPositionY();
        return wp;
    }

    private ResolvedWaypoint resolveFromEmployeeId(UUID companyId, UUID employeeId) {
        WorkplaceSeat seat = (companyId != null)
                ? seatRepository.findByCompanyIdAndAssignedEmployeeId(companyId, employeeId).orElse(null)
                : null;

        if (seat == null) {
            throw new AppException(WorkplaceErrorCode.EMPLOYEE_HAS_NO_ASSIGNED_SEAT);
        }

        return resolveFromSeatId(companyId, seat.getId());
    }

    // =========================================================================
    // SCENARIO HANDLERS
    // =========================================================================

    private NavigationRouteResponse buildCrossSiteResponse(ResolvedWaypoint origin, ResolvedWaypoint destination) {
        List<String> instructions = List.of(
                "Điểm xuất phát: " + origin.title + " tại cơ sở [" + origin.siteName + "].",
                "Điểm đích: " + destination.title + " tại cơ sở khác [" + destination.siteName + "].",
                "Vui lòng sử dụng phương tiện giao thông bên ngoài để di chuyển giữa hai cơ sở.",
                "Khi tới cơ sở [" + destination.siteName + "], tiếp tục sử dụng ứng dụng để chỉ đường nội bộ tới tầng " + destination.floorNumber + "."
        );

        return NavigationRouteResponse.builder()
                .navigationType(NavigationType.CROSS_SITE)
                .origin(toWaypointLocation(origin))
                .destination(toWaypointLocation(destination))
                .totalDistanceMeters(0.0)
                .estimatedWalkSeconds(0)
                .stepCount(instructions.size())
                .pathPoints(Collections.emptyList())
                .turnByTurnInstructions(instructions)
                .build();
    }

    private NavigationRouteResponse buildCrossFloorResponse(UUID companyId, ResolvedWaypoint origin, ResolvedWaypoint destination) {
        // Tìm thang máy hoặc cầu thang gần nhất trên tầng origin
        List<WorkplaceNonDeskObject> originElevators = nonDeskObjectRepository.findByFloorIdAndObjectTypeIn(
                origin.floorId, List.of(NonDeskObjectType.ELEVATOR, NonDeskObjectType.STAIRS)
        );

        WorkplaceNonDeskObject nearestElevator = findNearestObject(origin.x.doubleValue(), origin.y.doubleValue(), originElevators);

        List<Point2D> combinedPath = new ArrayList<>();
        List<String> instructions = new ArrayList<>();
        double totalDistance = 0.0;

        WorkplaceFloorPlan originFloorPlan = floorPlanRepository.findByFloorId(origin.floorId).orElse(null);
        double scaleRatio = (originFloorPlan != null && originFloorPlan.getScaleRatio() != null)
                ? originFloorPlan.getScaleRatio().doubleValue()
                : 0.05;

        if (nearestElevator != null) {
            // Chặng 1: Đi từ origin tới Thang máy tầng xuất phát
            List<AStarPathfinder.ObstacleRect> obstacles = collectObstacles(companyId, origin.floorId, origin.seatId, null);
            double canvasW = (originFloorPlan != null && originFloorPlan.getCanvasWidth() != null) ? originFloorPlan.getCanvasWidth().doubleValue() : 3000;
            double canvasH = (originFloorPlan != null && originFloorPlan.getCanvasHeight() != null) ? originFloorPlan.getCanvasHeight().doubleValue() : 2000;

            List<Point2D> path1 = pathfinder.findPath(
                    origin.x.doubleValue(), origin.y.doubleValue(),
                    nearestElevator.getPositionX().doubleValue(), nearestElevator.getPositionY().doubleValue(),
                    canvasW, canvasH, obstacles
            );

            combinedPath.addAll(path1);
            totalDistance += pathfinder.calculateDistanceMeters(path1, scaleRatio);

            instructions.add("Từ " + origin.title + ", di chuyển tới " + nearestElevator.getName() + " (Tầng " + origin.floorNumber + ").");
            instructions.add("Đi " + nearestElevator.getName() + " chuyển từ Tầng " + origin.floorNumber + " lên/xuống Tầng " + destination.floorNumber + ".");
            instructions.add("Ra khỏi thang máy tại Tầng " + destination.floorNumber + ", tiếp tục đi tới " + destination.title + " (" + destination.zoneName + ").");
        } else {
            instructions.add("Di chuyển tới thang máy/cầu thang gần nhất trên Tầng " + origin.floorNumber + ".");
            instructions.add("Di chuyển lên/xuống Tầng " + destination.floorNumber + ".");
            instructions.add("Tìm tới " + destination.title + " tại khu vực " + destination.zoneName + ".");
        }

        int walkSeconds = pathfinder.estimateWalkSeconds(totalDistance) + 90; // cộng thêm 90s thời gian chờ & đi thang máy

        return NavigationRouteResponse.builder()
                .navigationType(NavigationType.CROSS_FLOOR)
                .origin(toWaypointLocation(origin))
                .destination(toWaypointLocation(destination))
                .totalDistanceMeters(totalDistance)
                .estimatedWalkSeconds(walkSeconds)
                .stepCount(instructions.size())
                .pathPoints(combinedPath)
                .turnByTurnInstructions(instructions)
                .build();
    }

    private NavigationRouteResponse buildSameFloorResponse(UUID companyId, ResolvedWaypoint origin, ResolvedWaypoint destination) {
        WorkplaceFloorPlan floorPlan = floorPlanRepository.findByFloorId(origin.floorId).orElse(null);

        double canvasW = (floorPlan != null && floorPlan.getCanvasWidth() != null) ? floorPlan.getCanvasWidth().doubleValue() : 3000;
        double canvasH = (floorPlan != null && floorPlan.getCanvasHeight() != null) ? floorPlan.getCanvasHeight().doubleValue() : 2000;
        double scaleRatio = (floorPlan != null && floorPlan.getScaleRatio() != null) ? floorPlan.getScaleRatio().doubleValue() : 0.05;

        // Thu thập các vật cản để né (ngoại trừ điểm đầu và điểm cuối)
        List<AStarPathfinder.ObstacleRect> obstacles = collectObstacles(companyId, origin.floorId, origin.seatId, destination.seatId);

        List<Point2D> pathPoints = pathfinder.findPath(
                origin.x.doubleValue(), origin.y.doubleValue(),
                destination.x.doubleValue(), destination.y.doubleValue(),
                canvasW, canvasH, obstacles
        );

        double distanceMeters = pathfinder.calculateDistanceMeters(pathPoints, scaleRatio);
        int walkSeconds = pathfinder.estimateWalkSeconds(distanceMeters);
        List<String> instructions = pathfinder.generateTurnByTurnInstructions(pathPoints);

        // Bổ sung câu thông tin mở đầu và kết thúc
        List<String> fullInstructions = new ArrayList<>();
        fullInstructions.add("Bắt đầu từ " + origin.title + " (Khu " + (origin.zoneName != null ? origin.zoneName : "vực") + ").");
        fullInstructions.addAll(instructions);
        fullInstructions.add("Bạn đã đến nơi: " + destination.title + ".");

        return NavigationRouteResponse.builder()
                .navigationType(NavigationType.SAME_FLOOR)
                .origin(toWaypointLocation(origin))
                .destination(toWaypointLocation(destination))
                .totalDistanceMeters(distanceMeters)
                .estimatedWalkSeconds(walkSeconds)
                .stepCount(fullInstructions.size())
                .pathPoints(pathPoints)
                .turnByTurnInstructions(fullInstructions)
                .build();
    }

    // =========================================================================
    // OBSTACLE COLLECTION
    // =========================================================================

    private List<AStarPathfinder.ObstacleRect> collectObstacles(UUID companyId, UUID floorId, UUID excludeSeatId1, UUID excludeSeatId2) {
        List<AStarPathfinder.ObstacleRect> obstacles = new ArrayList<>();

        // 1. Thêm các bàn làm việc trên tầng làm vật cản
        List<WorkplaceSeat> seats = seatRepository.findByFloorIdWithHierarchy(floorId);
        for (WorkplaceSeat seat : seats) {
            if (seat.getId().equals(excludeSeatId1) || seat.getId().equals(excludeSeatId2)) {
                continue;
            }
            if (seat.getPositionX() != null && seat.getPositionY() != null) {
                double w = seat.getWidth() != null ? seat.getWidth().doubleValue() : 120.0;
                double h = seat.getHeight() != null ? seat.getHeight().doubleValue() : 60.0;
                obstacles.add(new AStarPathfinder.ObstacleRect(
                        seat.getPositionX().doubleValue() - w / 2.0,
                        seat.getPositionY().doubleValue() - h / 2.0,
                        w, h
                ));
            }
        }

        // 2. Thêm các phòng họp và vách tường làm vật cản
        List<WorkplaceNonDeskObject> objects = nonDeskObjectRepository.findByFloorId(floorId);
        for (WorkplaceNonDeskObject obj : objects) {
            // Cửa ra vào (ENTRANCE / EXIT) không tính là vật cản chặn hoàn toàn
            if (obj.getObjectType() == NonDeskObjectType.ENTRANCE || obj.getObjectType() == NonDeskObjectType.EXIT) {
                continue;
            }
            if (obj.getPositionX() != null && obj.getPositionY() != null) {
                double w = obj.getWidth() != null ? obj.getWidth().doubleValue() : 150.0;
                double h = obj.getHeight() != null ? obj.getHeight().doubleValue() : 150.0;
                obstacles.add(new AStarPathfinder.ObstacleRect(
                        obj.getPositionX().doubleValue() - w / 2.0,
                        obj.getPositionY().doubleValue() - h / 2.0,
                        w, h
                ));
            }
        }

        return obstacles;
    }

    private WorkplaceNonDeskObject findNearestObject(double x, double y, List<WorkplaceNonDeskObject> objects) {
        WorkplaceNonDeskObject nearest = null;
        double minDistanceSq = Double.MAX_VALUE;
        for (WorkplaceNonDeskObject obj : objects) {
            if (obj.getPositionX() == null || obj.getPositionY() == null) continue;
            double dx = obj.getPositionX().doubleValue() - x;
            double dy = obj.getPositionY().doubleValue() - y;
            double distSq = dx * dx + dy * dy;
            if (distSq < minDistanceSq) {
                minDistanceSq = distSq;
                nearest = obj;
            }
        }
        return nearest;
    }

    private WaypointLocation toWaypointLocation(ResolvedWaypoint wp) {
        return WaypointLocation.builder()
                .siteId(wp.siteId)
                .siteName(wp.siteName)
                .floorId(wp.floorId)
                .floorNumber(wp.floorNumber)
                .zoneId(wp.zoneId)
                .zoneName(wp.zoneName)
                .rowId(wp.rowId)
                .rowName(wp.rowName)
                .seatId(wp.seatId)
                .seatCode(wp.seatCode)
                .targetTitle(wp.title)
                .positionX(wp.x)
                .positionY(wp.y)
                .build();
    }
}
