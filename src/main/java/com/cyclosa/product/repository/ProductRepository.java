package com.cyclosa.product.repository;

import com.cyclosa.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {

    boolean existsBySkuAndDeletedFalse(String sku);

    boolean existsBySkuAndIdNotAndDeletedFalse(String sku, UUID id);

    Optional<Product> findByIdAndDeletedFalse(UUID id);
}

