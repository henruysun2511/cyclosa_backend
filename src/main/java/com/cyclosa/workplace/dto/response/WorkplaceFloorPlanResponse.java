package com.cyclosa.workplace.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkplaceFloorPlanResponse {

    private UUID id;
    private UUID companyId;
    private UUID floorId;
    private String floorNumber;
    private String backgroundImageUrl;
    private BigDecimal canvasWidth;
    private BigDecimal canvasHeight;
    private BigDecimal scaleRatio;
    private Integer version;
    private Boolean isPublished;
    private Instant createdAt;
    private Instant updatedAt;
}
