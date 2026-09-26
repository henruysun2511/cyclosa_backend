package com.cyclosa.workplace.dto.response;

import com.cyclosa.asset.dto.response.AssetDetailResponse;
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
public class WorkplaceSeatDetailResponse {

    private UUID id;
    private UUID companyId;

    // Phân cấp 5 tầng
    private UUID siteId;
    private String siteName;
    private String siteCode;
    private UUID floorId;
    private String floorNumber;
    private UUID zoneId;
    private String zoneName;
    private UUID rowId;
    private String rowName;

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

    // Nhân viên gán cố định
    private UUID assignedEmployeeId;
    private EmployeeSummary assignedEmployee;

    // Mã QR & đồ họa
    private String qrCodeData;
    private String visualMetadata;

    // Danh sách các tài sản gắn liền tại bàn (Module 13 Asset)
    private List<AssetDetailResponse> linkedAssets;

    // Lịch đặt hot-desk trong ngày hôm nay (nếu có)
    private HotdeskBookingResponse todayBooking;

    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;
}
