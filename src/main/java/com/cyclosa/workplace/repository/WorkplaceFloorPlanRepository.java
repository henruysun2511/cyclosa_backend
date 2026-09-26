package com.cyclosa.workplace.repository;

import com.cyclosa.workplace.entity.WorkplaceFloorPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkplaceFloorPlanRepository extends JpaRepository<WorkplaceFloorPlan, UUID> {

    @Query("SELECT p FROM WorkplaceFloorPlan p " +
           "JOIN FETCH p.floor f " +
           "JOIN FETCH f.site " +
           "WHERE p.id = :id AND p.companyId = :companyId")
    Optional<WorkplaceFloorPlan> findByIdAndCompanyIdWithHierarchy(@Param("id") UUID id, @Param("companyId") UUID companyId);

    Optional<WorkplaceFloorPlan> findByIdAndCompanyId(UUID id, UUID companyId);

    @Query("SELECT p FROM WorkplaceFloorPlan p " +
           "WHERE p.floor.id = :floorId AND p.companyId = :companyId AND p.isPublished = true ORDER BY p.version DESC")
    List<WorkplaceFloorPlan> findPublishedByFloorId(@Param("floorId") UUID floorId, @Param("companyId") UUID companyId);

    @Query("SELECT p FROM WorkplaceFloorPlan p " +
           "WHERE p.floor.id = :floorId AND p.companyId = :companyId ORDER BY p.version DESC")
    List<WorkplaceFloorPlan> findAllVersionsByFloorId(@Param("floorId") UUID floorId, @Param("companyId") UUID companyId);

    Optional<WorkplaceFloorPlan> findByFloorId(UUID floorId);

    Optional<WorkplaceFloorPlan> findByFloorIdAndIsPublishedTrue(UUID floorId);

    @Query("SELECT MAX(p.version) FROM WorkplaceFloorPlan p WHERE p.floor.id = :floorId AND p.companyId = :companyId")
    Integer findMaxVersionByFloorId(@Param("floorId") UUID floorId, @Param("companyId") UUID companyId);
}
