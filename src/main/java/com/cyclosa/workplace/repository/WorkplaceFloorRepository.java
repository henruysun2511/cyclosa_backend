package com.cyclosa.workplace.repository;

import com.cyclosa.workplace.entity.WorkplaceFloor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkplaceFloorRepository extends JpaRepository<WorkplaceFloor, UUID> {

    @Query("SELECT f FROM WorkplaceFloor f JOIN FETCH f.site WHERE f.id = :id AND f.companyId = :companyId")
    Optional<WorkplaceFloor> findByIdAndCompanyIdWithSite(@Param("id") UUID id, @Param("companyId") UUID companyId);

    Optional<WorkplaceFloor> findByIdAndCompanyId(UUID id, UUID companyId);

    boolean existsByCompanyIdAndSiteIdAndFloorNumber(UUID companyId, UUID siteId, String floorNumber);

    boolean existsByCompanyIdAndSiteIdAndFloorNumberAndIdNot(UUID companyId, UUID siteId, String floorNumber, UUID id);

    @Query("SELECT f FROM WorkplaceFloor f JOIN FETCH f.site WHERE f.site.id = :siteId AND f.companyId = :companyId ORDER BY f.floorNumber ASC")
    List<WorkplaceFloor> findBySiteIdAndCompanyId(@Param("siteId") UUID siteId, @Param("companyId") UUID companyId);

    @Query("SELECT f FROM WorkplaceFloor f JOIN FETCH f.site WHERE f.companyId = :companyId ORDER BY f.site.name ASC, f.floorNumber ASC")
    List<WorkplaceFloor> findByCompanyId(@Param("companyId") UUID companyId);

    @Query("SELECT f FROM WorkplaceFloor f JOIN FETCH f.site WHERE f.site.id = :siteId ORDER BY f.floorNumber ASC")
    List<WorkplaceFloor> findBySiteIdOrderByFloorNumberAsc(@Param("siteId") UUID siteId);
}
