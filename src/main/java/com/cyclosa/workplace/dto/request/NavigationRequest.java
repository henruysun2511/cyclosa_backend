package com.cyclosa.workplace.dto.request;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NavigationRequest {

    // Điểm xuất phát (Origin)
    private UUID originEmployeeId;
    private UUID originSeatId;
    private UUID originObjectId;

    // Điểm đến (Destination)
    private UUID destinationEmployeeId;
    private UUID destinationSeatId;
    private UUID destinationObjectId;
}
