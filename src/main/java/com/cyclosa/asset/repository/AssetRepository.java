package com.cyclosa.asset.repository;

import com.cyclosa.asset.entity.Asset;
import com.cyclosa.asset.enums.AssetCategory;
import com.cyclosa.asset.enums.AssetStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AssetRepository extends JpaRepository<Asset, UUID>, JpaSpecificationExecutor<Asset> {

    boolean existsByAssetCodeAndCompanyId(String assetCode, UUID companyId);

    Optional<Asset> findByAssetCodeAndCompanyId(String assetCode, UUID companyId);

    Optional<Asset> findByIdAndCompanyId(UUID id, UUID companyId);

    Page<Asset> findByCompanyId(UUID companyId, Pageable pageable);

    @Query("SELECT a FROM Asset a WHERE a.companyId = :companyId " +
           "AND (:category IS NULL OR a.category = :category) " +
           "AND (:status IS NULL OR a.status = :status) " +
           "AND (:keyword IS NULL OR LOWER(a.assetCode) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "     OR LOWER(a.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "     OR LOWER(a.serialNumber) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Asset> searchAssets(
            @Param("companyId") UUID companyId,
            @Param("category") AssetCategory category,
            @Param("status") AssetStatus status,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}
