package com.cyclosa.asset.dto.request;

import com.cyclosa.asset.enums.AssetStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangeAssetStatusRequest {

    @NotNull(message = "Trạng thái tài sản mới không được để trống")
    private AssetStatus status;

    private String note;
}
