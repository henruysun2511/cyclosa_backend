package com.cyclosa.asset.dto.request;

import com.cyclosa.asset.enums.AssetCondition;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReturnAssetRequest {

    @NotNull(message = "Ngày thu hồi không được để trống")
    private LocalDate returnedDate;

    @NotNull(message = "Tình trạng tài sản khi thu hồi không được để trống")
    private AssetCondition conditionOnReturn;

    private String note;
}
