package com.cyclosa.asset.repository;

import com.cyclosa.asset.entity.AssetInventoryCheck;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AssetInventoryCheckRepository extends JpaRepository<AssetInventoryCheck, UUID> {

    Page<AssetInventoryCheck> findByCompanyId(UUID companyId, Pageable pageable);

    Optional<AssetInventoryCheck> findByIdAndCompanyId(UUID id, UUID companyId);
}
