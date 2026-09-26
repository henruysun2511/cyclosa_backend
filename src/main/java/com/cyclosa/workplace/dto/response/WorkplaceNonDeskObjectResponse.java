package com.cyclosa.workplace.dto.response;

import com.cyclosa.workplace.enums.NonDeskObjectType;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkplaceNonDeskObjectResponse {

    private UUID id;
    private UUID companyId;
    private UUID zoneId;
    private String zoneName;
    private UUID floorId;
    private String floorNumber;
    private NonDeskObjectType objectType;
    private String name;
    private BigDecimal positionX;
    private BigDecimal positionY;
    private BigDecimal width;
    private BigDecimal height;
    private BigDecimal rotation;
    private Integer meetingRoomCapacity;
    private String qrCodeData;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;
}
