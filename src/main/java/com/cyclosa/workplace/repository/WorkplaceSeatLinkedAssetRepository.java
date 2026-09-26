package com.cyclosa.workplace.repository;

import com.cyclosa.workplace.entity.WorkplaceSeatLinkedAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface WorkplaceSeatLinkedAssetRepository extends JpaRepository<WorkplaceSeatLinkedAsset, UUID> {

    List<WorkplaceSeatLinkedAsset> findByCompanyIdAndSeatId(UUID companyId, UUID seatId);

    @Query("SELECT a FROM WorkplaceSeatLinkedAsset a WHERE a.seat.id IN :seatIds AND a.companyId = :companyId")
    List<WorkplaceSeatLinkedAsset> findBySeatIdInAndCompanyId(@Param("seatIds") Set<UUID> seatIds, @Param("companyId") UUID companyId);

    Optional<WorkplaceSeatLinkedAsset> findByCompanyIdAndSeatIdAndAssetId(UUID companyId, UUID seatId, UUID assetId);

    boolean existsByCompanyIdAndSeatIdAndAssetId(UUID companyId, UUID seatId, UUID assetId);

    void deleteByCompanyIdAndSeatIdAndAssetId(UUID companyId, UUID seatId, UUID assetId);

    void deleteByCompanyIdAndSeatId(UUID companyId, UUID seatId);
}
