package com.cyclosa.asset.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Yêu cầu cấp phát tài sản cho nhân viên")
public class CreateAssetAssignmentRequest extends AllocateAssetRequest {

    @NotNull(message = "ID tài sản không được để trống")
    @Schema(description = "ID tài sản cần cấp phát")
    private UUID assetId;
}
