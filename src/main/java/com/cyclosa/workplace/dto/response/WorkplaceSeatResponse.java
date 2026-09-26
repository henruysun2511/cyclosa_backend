package com.cyclosa.workplace.dto.response;

import com.cyclosa.common.dto.summary.EmployeeSummary;
import com.cyclosa.workplace.enums.DeskShape;
import com.cyclosa.workplace.enums.SeatStatus;
import com.cyclosa.workplace.enums.SeatType;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkplaceSeatResponse {

    private UUID id;
    private UUID companyId;
    private UUID rowId;
    private String rowName;
    private UUID zoneId;
    private String zoneName;
    private UUID floorId;
    private String floorNumber;
    private UUID siteId;
    private String siteName;
    private String seatCode;
    private SeatType seatType;
    private SeatStatus status;
    private DeskShape deskShape;
    private BigDecimal positionX;
    private BigDecimal positionY;
    private BigDecimal width;
    private BigDecimal height;
    private BigDecimal rotation;
    private List<String> amenities;
    private UUID assignedEmployeeId;
    private EmployeeSummary assignedEmployee;
    private String qrCodeData;
    private String visualMetadata;
    private int linkedAssetCount;
    private Instant createdAt;
    private Instant updatedAt;
}
