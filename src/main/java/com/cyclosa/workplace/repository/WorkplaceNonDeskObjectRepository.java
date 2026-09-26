package com.cyclosa.workplace.repository;

import com.cyclosa.workplace.entity.WorkplaceNonDeskObject;
import com.cyclosa.workplace.enums.NonDeskObjectType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkplaceNonDeskObjectRepository extends JpaRepository<WorkplaceNonDeskObject, UUID> {

    @Query("SELECT o FROM WorkplaceNonDeskObject o " +
           "JOIN FETCH o.zone z " +
           "JOIN FETCH z.floor f " +
           "JOIN FETCH f.site " +
           "WHERE o.id = :id AND o.companyId = :companyId")
    Optional<WorkplaceNonDeskObject> findByIdAndCompanyIdWithHierarchy(@Param("id") UUID id, @Param("companyId") UUID companyId);

    Optional<WorkplaceNonDeskObject> findByIdAndCompanyId(UUID id, UUID companyId);

    @Query("SELECT o FROM WorkplaceNonDeskObject o " +
           "JOIN FETCH o.zone z " +
           "WHERE z.floor.id = :floorId AND o.companyId = :companyId ORDER BY o.name ASC")
    List<WorkplaceNonDeskObject> findByFloorIdAndCompanyId(@Param("floorId") UUID floorId, @Param("companyId") UUID companyId);

    @Query("SELECT o FROM WorkplaceNonDeskObject o " +
           "JOIN FETCH o.zone z " +
           "WHERE z.floor.id = :floorId AND o.objectType = :objectType AND o.companyId = :companyId")
    List<WorkplaceNonDeskObject> findByFloorIdAndObjectTypeAndCompanyId(@Param("floorId") UUID floorId,
                                                                       @Param("objectType") NonDeskObjectType objectType,
                                                                       @Param("companyId") UUID companyId);

    @Query("SELECT o FROM WorkplaceNonDeskObject o " +
           "JOIN FETCH o.zone z " +
           "WHERE z.floor.id = :floorId ORDER BY o.name ASC")
    List<WorkplaceNonDeskObject> findByFloorId(@Param("floorId") UUID floorId);

    @Query("SELECT o FROM WorkplaceNonDeskObject o " +
           "JOIN FETCH o.zone z " +
           "WHERE z.floor.id = :floorId AND o.objectType = :objectType")
    List<WorkplaceNonDeskObject> findByFloorIdAndObjectType(@Param("floorId") UUID floorId, @Param("objectType") NonDeskObjectType objectType);

    @Query("SELECT o FROM WorkplaceNonDeskObject o " +
           "JOIN FETCH o.zone z " +
           "WHERE o.objectType = :objectType")
    List<WorkplaceNonDeskObject> findByObjectType(@Param("objectType") NonDeskObjectType objectType);

    @Query("SELECT o FROM WorkplaceNonDeskObject o " +
           "JOIN FETCH o.zone z " +
           "WHERE z.floor.id = :floorId AND o.objectType IN :types")
    List<WorkplaceNonDeskObject> findByFloorIdAndObjectTypeIn(@Param("floorId") UUID floorId, @Param("types") java.util.Collection<NonDeskObjectType> types);

    Optional<WorkplaceNonDeskObject> findByCompanyIdAndQrCodeData(UUID companyId, String qrCodeData);
}
