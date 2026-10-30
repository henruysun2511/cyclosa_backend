package com.cyclosa.product.dto.request;

import com.cyclosa.common.dto.request.BaseFilterRequest;
import com.cyclosa.product.enums.ProductStatus;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Set;

@Getter
@Setter
@Schema(description = "Bộ lọc và phân trang danh sách sản phẩm")
public class ProductFilterRequest extends BaseFilterRequest {

    public static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "name", "sku", "category", "price", "stockQuantity", "status", "createdAt"
    );

    @Parameter(description = "Lọc theo danh mục sản phẩm", example = "Laptop")
    private String category;

    @Parameter(description = "Lọc theo trạng thái sản phẩm", example = "ACTIVE")
    private ProductStatus status;

    @Parameter(description = "Lọc theo mức giá tối thiểu (>= minPrice)", example = "10000000.00")
    private BigDecimal minPrice;

    @Parameter(description = "Lọc theo mức giá tối đa (<= maxPrice)", example = "100000000.00")
    private BigDecimal maxPrice;
}

