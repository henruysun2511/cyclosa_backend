package com.cyclosa.workplace.dto.response;

import com.cyclosa.workplace.enums.NavigationType;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NavigationRouteResponse {

    private NavigationType navigationType; // SAME_FLOOR, CROSS_FLOOR, CROSS_SITE

    private WaypointLocation origin;
    private WaypointLocation destination;

    // Các thông số đường đi
    private Double totalDistanceMeters;
    private Integer estimatedWalkSeconds;
    private Integer stepCount;

    // Danh sách tọa độ các điểm vẽ đường (A* algorithm)
    private List<Point2D> pathPoints;

    // Chỉ dẫn từng bước bằng lời
    private List<String> turnByTurnInstructions;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WaypointLocation {
        private UUID siteId;
        private String siteName;
        private UUID floorId;
        private String floorNumber;
        private UUID zoneId;
        private String zoneName;
        private UUID rowId;
        private String rowName;
        private UUID seatId;
        private String seatCode;
        private String targetTitle;
        private BigDecimal positionX;
        private BigDecimal positionY;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Point2D {
        private BigDecimal x;
        private BigDecimal y;
    }
}
