package com.cyclosa.asset.dto.request;

import com.cyclosa.asset.enums.InventoryResult;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryCheckItemRequest {

    @NotNull(message = "ID tài sản không được để trống")
    private UUID assetId;

    @NotNull(message = "Kết quả kiểm kê không được để trống")
    private InventoryResult actualResult;

    private String note;
}
