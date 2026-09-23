package com.cyclosa.asset.dto.response;

import com.cyclosa.asset.enums.AssetCategory;
import com.cyclosa.asset.enums.AssetCondition;
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
public class AssetAllocationResponse {

    private UUID id;
    private UUID assetId;
    private String assetCode;
    private String assetName;
    private AssetCategory assetCategory;
    private String serialNumber;

    private UUID employeeId;
    private String employeeName;
    private String employeeCode;

    private UUID onboardingProcessId;
    private LocalDate allocatedDate;
    private LocalDate returnedDate;
    private AssetCondition conditionOnAllocation;
    private AssetCondition conditionOnReturn;
    private String note;

    private LocalDateTime createdAt;
}
