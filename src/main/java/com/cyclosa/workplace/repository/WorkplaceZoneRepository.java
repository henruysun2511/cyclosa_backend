package com.cyclosa.workplace.repository;

import com.cyclosa.workplace.entity.WorkplaceZone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkplaceZoneRepository extends JpaRepository<WorkplaceZone, UUID> {

    @Query("SELECT z FROM WorkplaceZone z JOIN FETCH z.floor f JOIN FETCH f.site WHERE z.id = :id AND z.companyId = :companyId")
    Optional<WorkplaceZone> findByIdAndCompanyIdWithHierarchy(@Param("id") UUID id, @Param("companyId") UUID companyId);

    Optional<WorkplaceZone> findByIdAndCompanyId(UUID id, UUID companyId);

    @Query("SELECT z FROM WorkplaceZone z JOIN FETCH z.floor f JOIN FETCH f.site WHERE z.floor.id = :floorId AND z.companyId = :companyId ORDER BY z.name ASC")
    List<WorkplaceZone> findByFloorIdAndCompanyId(@Param("floorId") UUID floorId, @Param("companyId") UUID companyId);

    List<WorkplaceZone> findByFloorIdOrderByZoneCodeAsc(UUID floorId);

    List<WorkplaceZone> findByFloorIdOrderByNameAsc(UUID floorId);

    List<WorkplaceZone> findByCompanyIdAndOrganizationalUnitId(UUID companyId, UUID organizationalUnitId);
}
