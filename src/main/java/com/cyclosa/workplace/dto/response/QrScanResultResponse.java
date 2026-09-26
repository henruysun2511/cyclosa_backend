package com.cyclosa.workplace.dto.response;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QrScanResultResponse {

    private String targetType; // "SEAT", "MEETING_ROOM", "WAYPOINT"
    private UUID targetId;
    private String targetCode;
    private String targetName;
    private String floorNumber;
    private String zoneName;
    private String actionSuggestion; // "CHECK_IN_SEAT", "BOOK_ROOM_QUICK", "WAYFINDING_ORIGIN"
    private Object details; // WorkplaceSeatResponse hoặc WorkplaceNonDeskObjectResponse
}
