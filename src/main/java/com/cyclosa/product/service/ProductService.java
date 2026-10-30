package com.cyclosa.product.service;

import com.cyclosa.common.exception.AppException;
import com.cyclosa.common.response.PageData;
import com.cyclosa.product.dto.request.CreateProductRequest;
import com.cyclosa.product.dto.request.ProductFilterRequest;
import com.cyclosa.product.dto.request.UpdateProductRequest;
import com.cyclosa.product.dto.response.ProductDetailResponse;
import com.cyclosa.product.dto.response.ProductResponse;
import com.cyclosa.product.entity.Product;
import com.cyclosa.product.enums.ProductStatus;
import com.cyclosa.product.mapper.ProductMapper;
import com.cyclosa.product.repository.ProductRepository;
import com.cyclosa.product.repository.specification.ProductSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public PageData<ProductResponse> getProducts(ProductFilterRequest filter) {
        log.debug("Lấy danh sách sản phẩm với bộ lọc: keyword={}, category={}, status={}",
                filter.getKeyword(), filter.getCategory(), filter.getStatus());

        Pageable pageable = filter.toPageable("createdAt", ProductFilterRequest.ALLOWED_SORT_FIELDS);
        Specification<Product> spec = ProductSpecification.build(filter);

        Page<Product> page = productRepository.findAll(spec, pageable);
        return PageData.of(page, productMapper::toResponse);
    }

    public ProductDetailResponse getProductById(UUID id) {
        log.debug("Lấy thông tin chi tiết sản phẩm id={}", id);
        Product product = findProductOrThrow(id);
        return productMapper.toDetailResponse(product);
    }

    @Transactional
    public ProductDetailResponse createProduct(CreateProductRequest request) {
        log.info("Tạo mới sản phẩm SKU: {}", request.getSku());

        // 1. Kiểm tra trùng lặp SKU trong các sản phẩm chưa bị xóa
        if (productRepository.existsBySkuAndDeletedFalse(request.getSku().trim())) {
            throw AppException.conflict("Mã SKU '" + request.getSku() + "' đã tồn tại trong hệ thống");
        }

        // 2. Chuyển DTO sang Entity & gán mặc định nếu thiếu
        Product product = productMapper.toEntity(request);
        product.setSku(request.getSku().trim());
        if (product.getStatus() == null) {
            product.setStatus(ProductStatus.ACTIVE);
        }
        product.setDeleted(false);

        // 3. Lưu vào Database
        Product saved = productRepository.save(product);
        log.info("Đã tạo sản phẩm thành công với id={}", saved.getId());

        return productMapper.toDetailResponse(saved);
    }

    @Transactional
    public ProductDetailResponse updateProduct(UUID id, UpdateProductRequest request) {
        log.info("Cập nhật thông tin sản phẩm id={}", id);
        Product product = findProductOrThrow(id);

        // 1. Kiểm tra SKU nếu bị trùng với sản phẩm khác
        if (productRepository.existsBySkuAndIdNotAndDeletedFalse(request.getSku().trim(), id)) {
            throw AppException.conflict("Mã SKU '" + request.getSku() + "' đã được sử dụng bởi sản phẩm khác");
        }

        // 2. Cập nhật dữ liệu từ DTO vào Entity
        productMapper.updateEntityFromDto(request, product);
        product.setSku(request.getSku().trim());

        // 3. Lưu bản ghi
        Product updated = productRepository.save(product);
        log.info("Cập nhật thành công sản phẩm id={}", updated.getId());

        return productMapper.toDetailResponse(updated);
    }

    @Transactional
    public void deleteProduct(UUID id) {
        log.warn("Thực hiện xóa mềm sản phẩm id={}", id);
        Product product = findProductOrThrow(id);

        // Cập nhật trạng thái xóa mềm
        product.setDeleted(true);
        product.setDeletedAt(LocalDateTime.now());
        productRepository.save(product);

        log.info("Đã xóa mềm thành công sản phẩm id={}", id);
    }

    private Product findProductOrThrow(UUID id) {
        return productRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> AppException.notFound("Không tìm thấy sản phẩm với id: " + id));
    }
}
