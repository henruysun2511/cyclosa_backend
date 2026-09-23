package com.cyclosa.asset.dto.response;

import com.cyclosa.asset.enums.AssetCategory;
import com.cyclosa.asset.enums.InventoryResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryCheckItemResponse {

    private UUID id;
    private UUID assetId;
    private String assetCode;
    private String assetName;
    private AssetCategory assetCategory;
    private String serialNumber;
    private InventoryResult actualResult;
    private String note;
}
