package com.cyclosa.asset.dto.request;

import com.cyclosa.asset.enums.AssetCategory;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAssetRequest {

    @NotBlank(message = "Tên tài sản không được để trống")
    @Size(max = 255, message = "Tên tài sản tối đa 255 ký tự")
    private String name;

    @NotNull(message = "Danh mục tài sản không được để trống")
    private AssetCategory category;

    private LocalDate purchaseDate;

    @DecimalMin(value = "0.0", inclusive = true, message = "Giá mua không được âm")
    private BigDecimal purchaseCost;

    @Size(max = 100, message = "Số serial tối đa 100 ký tự")
    private String serialNumber;

    private String specifications;

    private LocalDate warrantyExpiryDate;

    @Size(max = 255, message = "Vị trí lưu trữ tối đa 255 ký tự")
    private String location;

    private String note;
}
