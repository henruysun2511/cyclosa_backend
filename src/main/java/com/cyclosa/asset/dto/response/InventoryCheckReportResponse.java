package com.cyclosa.asset.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryCheckReportResponse {

    private UUID id;
    private UUID companyId;
    private String title;
    private LocalDate checkDate;
    private UUID performedByEmployeeId;
    private String performedByEmployeeName;
    private String performedByEmployeeCode;

    private int totalItems;
    private int matchedCount;
    private int missingCount;
    private int damagedCount;
    private double matchPercentage;

    private String note;
    private LocalDateTime createdAt;

    @Builder.Default
    private List<InventoryCheckItemResponse> items = new ArrayList<>();
}
