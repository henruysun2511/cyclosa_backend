package com.cyclosa.asset.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryCheckResponse {

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

    private String note;
    private LocalDateTime createdAt;
}
