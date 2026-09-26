package com.cyclosa.workplace.repository;

import com.cyclosa.workplace.entity.WorkplaceRow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkplaceRowRepository extends JpaRepository<WorkplaceRow, UUID> {

    @Query("SELECT r FROM WorkplaceRow r JOIN FETCH r.zone z JOIN FETCH z.floor f JOIN FETCH f.site WHERE r.id = :id AND r.companyId = :companyId")
    Optional<WorkplaceRow> findByIdAndCompanyIdWithHierarchy(@Param("id") UUID id, @Param("companyId") UUID companyId);

    Optional<WorkplaceRow> findByIdAndCompanyId(UUID id, UUID companyId);

    @Query("SELECT r FROM WorkplaceRow r JOIN FETCH r.zone WHERE r.zone.id = :zoneId AND r.companyId = :companyId ORDER BY r.name ASC")
    List<WorkplaceRow> findByZoneIdAndCompanyId(@Param("zoneId") UUID zoneId, @Param("companyId") UUID companyId);

    @Query("SELECT r FROM WorkplaceRow r JOIN FETCH r.zone z WHERE z.floor.id = :floorId AND r.companyId = :companyId")
    List<WorkplaceRow> findByFloorIdAndCompanyId(@Param("floorId") UUID floorId, @Param("companyId") UUID companyId);

    List<WorkplaceRow> findByZoneIdInOrderByRowCodeAsc(java.util.Collection<UUID> zoneIds);

    List<WorkplaceRow> findByZoneIdInOrderByNameAsc(java.util.Collection<UUID> zoneIds);
}
