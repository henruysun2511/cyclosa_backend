package com.cyclosa.workplace.dto.response;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkplaceFloorResponse {

    private UUID id;
    private UUID companyId;
    private UUID siteId;
    private String siteName;
    private String siteCode;
    private String floorNumber;
    private String name;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;
}
