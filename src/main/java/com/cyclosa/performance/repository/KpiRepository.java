package com.cyclosa.performance.repository;

import com.cyclosa.performance.entity.Kpi;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface KpiRepository extends JpaRepository<Kpi, UUID> {

    @Query("SELECT k FROM Kpi k WHERE " +
           "(:companyId IS NULL OR k.companyId = :companyId) AND " +
           "(:unitId IS NULL OR k.organizationalUnitId = :unitId OR k.organizationalUnitId IS NULL) AND " +
           "(:isActive IS NULL OR k.isActive = :isActive) AND " +
           "(:search IS NULL OR LOWER(k.name) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Kpi> searchKpis(
            @Param("companyId") UUID companyId,
            @Param("unitId") UUID unitId,
            @Param("isActive") Boolean isActive,
            @Param("search") String search,
            Pageable pageable
    );

    List<Kpi> findByOrganizationalUnitIdOrOrganizationalUnitIdIsNull(UUID organizationalUnitId);
}
