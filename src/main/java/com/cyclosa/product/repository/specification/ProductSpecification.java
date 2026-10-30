package com.cyclosa.product.repository.specification;

import com.cyclosa.product.dto.request.ProductFilterRequest;
import com.cyclosa.product.entity.Product;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class ProductSpecification {

    private ProductSpecification() {}

    public static Specification<Product> build(ProductFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Lọc từ khóa tìm kiếm (theo tên, SKU hoặc mô tả)
            if (filter.getKeyword() != null && !filter.getKeyword().isBlank()) {
                String pattern = "%" + filter.getKeyword().trim().toLowerCase() + "%";
                Predicate nameLike = cb.like(cb.lower(root.get("name")), pattern);
                Predicate skuLike = cb.like(cb.lower(root.get("sku")), pattern);
                Predicate descLike = cb.like(cb.lower(root.get("description")), pattern);
                predicates.add(cb.or(nameLike, skuLike, descLike));
            }

            // 2. Lọc theo danh mục
            if (filter.getCategory() != null && !filter.getCategory().isBlank()) {
                predicates.add(cb.equal(
                        cb.lower(root.get("category")),
                        filter.getCategory().trim().toLowerCase()
                ));
            }

            // 3. Lọc theo trạng thái
            if (filter.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), filter.getStatus()));
            }

            // 4. Lọc theo khoảng giá (minPrice, maxPrice)
            if (filter.getMinPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), filter.getMinPrice()));
            }
            if (filter.getMaxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), filter.getMaxPrice()));
            }

            // 5. Đảm bảo bản ghi chưa bị xóa mềm
            predicates.add(cb.isFalse(root.get("deleted")));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

