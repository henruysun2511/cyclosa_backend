package com.cyclosa.product.controller;

import com.cyclosa.common.response.ApiResponse;
import com.cyclosa.common.response.PageData;
import com.cyclosa.product.dto.request.CreateProductRequest;
import com.cyclosa.product.dto.request.ProductFilterRequest;
import com.cyclosa.product.dto.request.UpdateProductRequest;
import com.cyclosa.product.dto.response.ProductDetailResponse;
import com.cyclosa.product.dto.response.ProductResponse;
import com.cyclosa.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Product Sample Module", description = "Module mẫu chuẩn CRUD, lọc động, phân trang và xóa mềm")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @Operation(summary = "Lấy danh sách sản phẩm có lọc và phân trang",
               description = "Hỗ trợ tìm kiếm từ khóa, danh mục, trạng thái, khoảng giá, phân trang và sắp xếp")
    public ApiResponse<PageData<ProductResponse>> getProducts(@ParameterObject @Valid ProductFilterRequest filter) {
        PageData<ProductResponse> result = productService.getProducts(filter);
        return ApiResponse.ok(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy chi tiết sản phẩm theo ID")
    public ApiResponse<ProductDetailResponse> getProductById(
            @Parameter(description = "ID định danh của sản phẩm", required = true)
            @PathVariable UUID id) {
        ProductDetailResponse result = productService.getProductById(id);
        return ApiResponse.ok(result);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Tạo mới sản phẩm")
    public ApiResponse<ProductDetailResponse> createProduct(@Valid @RequestBody CreateProductRequest request) {
        ProductDetailResponse result = productService.createProduct(request);
        return ApiResponse.created(result);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật sản phẩm theo ID")
    public ApiResponse<ProductDetailResponse> updateProduct(
            @Parameter(description = "ID định danh của sản phẩm cần cập nhật", required = true)
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProductRequest request) {
        ProductDetailResponse result = productService.updateProduct(id, request);
        return ApiResponse.ok(result);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa mềm sản phẩm theo ID (chuyển deleted = true)")
    public ApiResponse<Void> deleteProduct(
            @Parameter(description = "ID định danh của sản phẩm cần xóa mềm", required = true)
            @PathVariable UUID id) {
        productService.deleteProduct(id);
        return ApiResponse.ok(null);
    }
}

