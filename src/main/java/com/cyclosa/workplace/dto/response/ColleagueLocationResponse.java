package com.cyclosa.workplace.dto.response;

import com.cyclosa.common.dto.summary.EmployeeSummary;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ColleagueLocationResponse {

    private UUID employeeId;
    private EmployeeSummary employee;

    // Vị trí 5 tầng
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

    // Tọa độ trên canvas
    private BigDecimal positionX;
    private BigDecimal positionY;
}
