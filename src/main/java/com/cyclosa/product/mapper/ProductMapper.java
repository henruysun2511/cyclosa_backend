package com.cyclosa.product.mapper;

import com.cyclosa.product.dto.request.CreateProductRequest;
import com.cyclosa.product.dto.request.UpdateProductRequest;
import com.cyclosa.product.dto.response.ProductDetailResponse;
import com.cyclosa.product.dto.response.ProductResponse;
import com.cyclosa.product.entity.Product;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        builder = @Builder(disableBuilder = true),
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ProductMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", constant = "false")
    @Mapping(target = "deletedAt", ignore = true)
    Product toEntity(CreateProductRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateEntityFromDto(UpdateProductRequest request, @MappingTarget Product product);

    ProductResponse toResponse(Product product);

    ProductDetailResponse toDetailResponse(Product product);
}
