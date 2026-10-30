package com.cyclosa.product.dto.response;

import com.cyclosa.product.enums.ProductStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin chi tiết sản phẩm")
public class ProductDetailResponse {

    @Schema(description = "ID định danh sản phẩm")
    private UUID id;

    @Schema(description = "Tên sản phẩm", example = "MacBook Pro M3 Max")
    private String name;

    @Schema(description = "Mã SKU", example = "MBP-M3MAX-16")
    private String sku;

    @Schema(description = "Danh mục", example = "Laptop")
    private String category;

    @Schema(description = "Đơn giá", example = "79990000.00")
    private BigDecimal price;

    @Schema(description = "Số lượng tồn kho", example = "50")
    private Integer stockQuantity;

    @Schema(description = "Mô tả chi tiết", example = "Cấu hình 36GB RAM, 1TB SSD...")
    private String description;

    @Schema(description = "Trạng thái", example = "ACTIVE")
    private ProductStatus status;

    @Schema(description = "Cờ xóa mềm", example = "false")
    private Boolean deleted;

    @Schema(description = "Thời điểm xóa mềm (nếu có)")
    private LocalDateTime deletedAt;

    @Schema(description = "Người tạo")
    private UUID createdBy;

    @Schema(description = "Người cập nhật gần nhất")
    private UUID updatedBy;

    @Schema(description = "Thời điểm tạo")
    private LocalDateTime createdAt;

    @Schema(description = "Thời điểm cập nhật gần nhất")
    private LocalDateTime updatedAt;
}

