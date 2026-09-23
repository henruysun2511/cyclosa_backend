package com.cyclosa.asset.dto.request;

import com.cyclosa.asset.enums.AssetCondition;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AllocateAssetRequest {

    @NotNull(message = "ID nhân viên không được để trống")
    private UUID employeeId;

    @NotNull(message = "Ngày cấp phát không được để trống")
    private LocalDate allocatedDate;

    @NotNull(message = "Tình trạng tài sản khi bàn giao không được để trống")
    private AssetCondition conditionOnAllocation;

    private UUID onboardingProcessId;

    private String note;
}
