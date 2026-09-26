package com.cyclosa.workplace.dto.response;

import com.cyclosa.workplace.enums.ClusterType;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkplaceRowResponse {

    private UUID id;
    private UUID companyId;
    private UUID zoneId;
    private String zoneName;
    private String name;
    private ClusterType clusterType;
    private BigDecimal positionX;
    private BigDecimal positionY;
    private BigDecimal rotation;
    private Instant createdAt;
    private Instant updatedAt;
}
