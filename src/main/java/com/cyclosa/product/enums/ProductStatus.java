package com.cyclosa.product.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Trạng thái sản phẩm")
public enum ProductStatus {
    @Schema(description = "Đang kinh doanh / Hoạt động")
    ACTIVE,

    @Schema(description = "Tạm ngừng kinh doanh")
    INACTIVE,

    @Schema(description = "Hết hàng")
    OUT_OF_STOCK
}

