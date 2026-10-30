package com.cyclosa.product;

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
import com.cyclosa.product.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductService productService;

    private UUID productId;
    private Product sampleProduct;
    private CreateProductRequest createRequest;
    private UpdateProductRequest updateRequest;
    private ProductResponse sampleResponse;
    private ProductDetailResponse sampleDetailResponse;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();

        sampleProduct = Product.builder()
                .name("MacBook Pro M3 Max")
                .sku("MBP-M3MAX")
                .category("Laptop")
                .price(new BigDecimal("79990000.00"))
                .stockQuantity(50)
                .description("Sample description")
                .status(ProductStatus.ACTIVE)
                .deleted(false)
                .build();
        sampleProduct.setId(productId);

        createRequest = CreateProductRequest.builder()
                .name("MacBook Pro M3 Max")
                .sku("MBP-M3MAX")
                .category("Laptop")
                .price(new BigDecimal("79990000.00"))
                .stockQuantity(50)
                .description("Sample description")
                .status(ProductStatus.ACTIVE)
                .build();

        updateRequest = UpdateProductRequest.builder()
                .name("MacBook Pro M3 Max Updated")
                .sku("MBP-M3MAX-NEW")
                .category("Laptop")
                .price(new BigDecimal("75000000.00"))
                .stockQuantity(40)
                .description("Updated description")
                .status(ProductStatus.ACTIVE)
                .build();

        sampleResponse = ProductResponse.builder()
                .id(productId)
                .name("MacBook Pro M3 Max")
                .sku("MBP-M3MAX")
                .category("Laptop")
                .price(new BigDecimal("79990000.00"))
                .stockQuantity(50)
                .status(ProductStatus.ACTIVE)
                .build();

        sampleDetailResponse = ProductDetailResponse.builder()
                .id(productId)
                .name("MacBook Pro M3 Max")
                .sku("MBP-M3MAX")
                .category("Laptop")
                .price(new BigDecimal("79990000.00"))
                .stockQuantity(50)
                .description("Sample description")
                .status(ProductStatus.ACTIVE)
                .deleted(false)
                .build();
    }

    @Test
    @DisplayName("Lấy danh sách sản phẩm có lọc và phân trang thành công")
    void getProducts_shouldReturnPagedData() {
        // Given
        ProductFilterRequest filter = new ProductFilterRequest();
        filter.setKeyword("MacBook");
        filter.setPage(0);
        filter.setSize(10);

        Page<Product> productPage = new PageImpl<>(List.of(sampleProduct));
        when(productRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(productPage);
        when(productMapper.toResponse(sampleProduct)).thenReturn(sampleResponse);

        // When
        PageData<ProductResponse> result = productService.getProducts(filter);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getName()).isEqualTo("MacBook Pro M3 Max");
        verify(productRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    @DisplayName("Lấy chi tiết sản phẩm thành công khi ID tồn tại")
    void getProductById_whenFound_shouldReturnDetail() {
        // Given
        when(productRepository.findByIdAndDeletedFalse(productId)).thenReturn(Optional.of(sampleProduct));
        when(productMapper.toDetailResponse(sampleProduct)).thenReturn(sampleDetailResponse);

        // When
        ProductDetailResponse result = productService.getProductById(productId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(productId);
        assertThat(result.getSku()).isEqualTo("MBP-M3MAX");
        verify(productRepository, times(1)).findByIdAndDeletedFalse(productId);
    }

    @Test
    @DisplayName("Ném ngoại lệ NotFoundException khi ID sản phẩm không tồn tại")
    void getProductById_whenNotFound_shouldThrowException() {
        // Given
        when(productRepository.findByIdAndDeletedFalse(productId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> productService.getProductById(productId))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Không tìm thấy sản phẩm");
    }

    @Test
    @DisplayName("Tạo mới sản phẩm thành công khi mã SKU chưa tồn tại")
    void createProduct_shouldSaveAndReturn() {
        // Given
        when(productRepository.existsBySkuAndDeletedFalse("MBP-M3MAX")).thenReturn(false);
        when(productMapper.toEntity(createRequest)).thenReturn(sampleProduct);
        when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);
        when(productMapper.toDetailResponse(sampleProduct)).thenReturn(sampleDetailResponse);

        // When
        ProductDetailResponse result = productService.createProduct(createRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getSku()).isEqualTo("MBP-M3MAX");
        verify(productRepository, times(1)).save(sampleProduct);
    }

    @Test
    @DisplayName("Ném ngoại lệ ConflictException khi tạo sản phẩm trùng mã SKU")
    void createProduct_whenDuplicateSku_shouldThrowException() {
        // Given
        when(productRepository.existsBySkuAndDeletedFalse("MBP-M3MAX")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> productService.createProduct(createRequest))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("đã tồn tại");
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Xóa mềm sản phẩm thành công khi gán cờ deleted = true")
    void deleteProduct_shouldSoftDelete() {
        // Given
        when(productRepository.findByIdAndDeletedFalse(productId)).thenReturn(Optional.of(sampleProduct));

        // When
        productService.deleteProduct(productId);

        // Then
        assertThat(sampleProduct.getDeleted()).isTrue();
        assertThat(sampleProduct.getDeletedAt()).isNotNull();
        verify(productRepository, times(1)).save(sampleProduct);
    }
}

