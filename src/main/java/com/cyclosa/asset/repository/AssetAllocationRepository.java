package com.cyclosa.asset.repository;

import com.cyclosa.asset.entity.AssetAllocation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AssetAllocationRepository extends JpaRepository<AssetAllocation, UUID>, JpaSpecificationExecutor<AssetAllocation> {

    Optional<AssetAllocation> findByAssetIdAndReturnedDateIsNull(UUID assetId);

    boolean existsByAssetIdAndReturnedDateIsNull(UUID assetId);

    Page<AssetAllocation> findByEmployeeId(UUID employeeId, Pageable pageable);

    List<AssetAllocation> findByEmployeeIdAndReturnedDateIsNull(UUID employeeId);

    @Query("SELECT COUNT(a) FROM AssetAllocation a WHERE a.employeeId = :employeeId AND a.returnedDate IS NULL")
    long countUnreturnedAssetsByEmployeeId(@Param("employeeId") UUID employeeId);

    List<AssetAllocation> findByAssetIdOrderByAllocatedDateDesc(UUID assetId);

    Optional<AssetAllocation> findByOnboardingProcessId(UUID onboardingProcessId);
}
