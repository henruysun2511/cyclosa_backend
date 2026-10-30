package com.cyclosa.product.dto.request;

import com.cyclosa.product.enums.ProductStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu tạo mới sản phẩm")
public class CreateProductRequest {

    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(max = 255, message = "Tên sản phẩm tối đa 255 ký tự")
    @Schema(description = "Tên sản phẩm", example = "MacBook Pro M3 Max 16 inch")
    private String name;

    @NotBlank(message = "Mã SKU không được để trống")
    @Size(max = 100, message = "Mã SKU tối đa 100 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "Mã SKU chỉ được chứa chữ cái, số, gạch ngang và gạch dưới")
    @Schema(description = "Mã quản lý kho (SKU duy nhất)", example = "MBP-M3MAX-16")
    private String sku;

    @NotBlank(message = "Danh mục không được để trống")
    @Size(max = 100, message = "Tên danh mục tối đa 100 ký tự")
    @Schema(description = "Danh mục sản phẩm", example = "Laptop")
    private String category;

    @NotNull(message = "Giá sản phẩm không được để trống")
    @DecimalMin(value = "0.00", inclusive = true, message = "Giá sản phẩm không được âm")
    @Digits(integer = 13, fraction = 2, message = "Giá sản phẩm không đúng định dạng tiền tệ")
    @Schema(description = "Đơn giá", example = "79990000.00")
    private BigDecimal price;

    @NotNull(message = "Số lượng tồn kho không được để trống")
    @Min(value = 0, message = "Số lượng tồn kho không được âm")
    @Schema(description = "Số lượng tồn kho ban đầu", example = "50")
    private Integer stockQuantity;

    @Size(max = 2000, message = "Mô tả sản phẩm tối đa 2000 ký tự")
    @Schema(description = "Mô tả chi tiết", example = "Cấu hình 36GB RAM, 1TB SSD")
    private String description;

    @Schema(description = "Trạng thái ban đầu", example = "ACTIVE")
    private ProductStatus status;
}

