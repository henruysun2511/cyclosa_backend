package com.cyclosa.onboarding.dto.request;

import com.cyclosa.onboarding.enums.ProvisioningStatus;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Yêu cầu cập nhật trạng thái cấp phát tài khoản")
public class UpdateAccountProvisioningRequest {

    @NotNull(message = "Trạng thái cấp phát không được để trống")
    @Schema(description = "Trạng thái mới: PROVISIONED hoặc REVOKED", example = "PROVISIONED")
    private ProvisioningStatus status;

    @Schema(description = "ID nhân viên IT thực hiện (mặc định lấy theo tài khoản hiện tại)")
    private UUID provisionedByEmployeeId;

    @Schema(description = "Ghi chú kết quả cấp phát")
    private String notes;
}
