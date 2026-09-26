package com.cyclosa.workplace.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FloorLayoutCanvasResponse {

    private UUID floorId;
    private String floorNumber;
    private String floorName;
    private UUID siteId;
    private String siteName;

    // Sơ đồ nền
    private String backgroundImageUrl;
    private BigDecimal canvasWidth;
    private BigDecimal canvasHeight;
    private BigDecimal scaleRatio;
    private Integer floorPlanVersion;

    // Dữ liệu các lớp tương tác
    private List<WorkplaceZoneResponse> zones;
    private List<WorkplaceRowResponse> rows;
    private List<WorkplaceSeatResponse> seats;
    private List<WorkplaceNonDeskObjectResponse> nonDeskObjects;
}
