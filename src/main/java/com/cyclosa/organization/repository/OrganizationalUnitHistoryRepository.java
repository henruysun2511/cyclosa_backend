package com.cyclosa.organization.repository;

import com.cyclosa.organization.entity.OrganizationalUnitHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrganizationalUnitHistoryRepository extends JpaRepository<OrganizationalUnitHistory, UUID> {

    @Query("SELECT h FROM OrganizationalUnitHistory h WHERE h.unitId = :unitId AND h.effectiveTo IS NULL")
    Optional<OrganizationalUnitHistory> findCurrentActive(@Param("unitId") UUID unitId);

    @Query("SELECT h FROM OrganizationalUnitHistory h " +
           "WHERE h.companyId = :companyId " +
           "AND h.effectiveFrom <= :timestamp " +
           "AND (h.effectiveTo IS NULL OR h.effectiveTo > :timestamp) " +
           "ORDER BY h.name ASC")
    List<OrganizationalUnitHistory> findByCompanyIdAtTimestamp(@Param("companyId") UUID companyId,
                                                              @Param("timestamp") LocalDateTime timestamp);

    List<OrganizationalUnitHistory> findByUnitIdOrderByEffectiveFromDesc(UUID unitId);
}
