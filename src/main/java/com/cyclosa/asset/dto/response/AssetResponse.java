package com.cyclosa.asset.dto.response;

import com.cyclosa.asset.enums.AssetCategory;
import com.cyclosa.asset.enums.AssetStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetResponse {

    private UUID id;
    private String assetCode;
    private String name;
    private AssetCategory category;
    private LocalDate purchaseDate;
    private BigDecimal purchaseCost;
    private AssetStatus status;
    private UUID companyId;
    private String serialNumber;
    private String location;
    private LocalDate warrantyExpiryDate;

    // Thông tin nhân viên đang giữ tài sản (nếu status == ALLOCATED)
    private UUID currentEmployeeId;
    private String currentEmployeeName;
    private String currentEmployeeCode;
    private LocalDate currentAllocatedDate;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
