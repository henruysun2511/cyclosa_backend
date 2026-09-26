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
public class WorkplaceOccupancyReportResponse {

    private long totalSeats;
    private long occupiedSeats;
    private long availableSeats;
    private long maintenanceSeats;
    private long blockedSeats;
    private BigDecimal occupancyRatePercentage; // (occupied / total) * 100

    private List<FloorOccupancyStats> floorStats;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FloorOccupancyStats {
        private UUID floorId;
        private String floorNumber;
        private String siteName;
        private long totalSeats;
        private long occupiedSeats;
        private long availableSeats;
        private BigDecimal occupancyRatePercentage;
    }
}
