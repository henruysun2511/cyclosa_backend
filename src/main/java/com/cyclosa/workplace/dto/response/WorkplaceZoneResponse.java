package com.cyclosa.workplace.dto.response;

import com.cyclosa.common.dto.summary.OrgUnitSummary;
import com.cyclosa.workplace.enums.ZoneShapeType;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkplaceZoneResponse {

    private UUID id;
    private UUID companyId;
    private UUID floorId;
    private String floorNumber;
    private String name;
    private UUID organizationalUnitId;
    private OrgUnitSummary organizationalUnit;
    private ZoneShapeType shapeType;
    private String boundaryCoordinates;
    private String colorCode;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;
}
