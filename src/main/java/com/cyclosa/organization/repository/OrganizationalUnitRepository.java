package com.cyclosa.organization.repository;

import com.cyclosa.organization.entity.OrganizationalUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrganizationalUnitRepository extends JpaRepository<OrganizationalUnit, UUID> {

    boolean existsByCodeAndCompanyId(String code, UUID companyId);

    boolean existsByCodeAndCompanyIdAndIdNot(String code, UUID companyId, UUID id);

    Optional<OrganizationalUnit> findByIdAndCompanyId(UUID id, UUID companyId);

    long countByCompanyId(UUID companyId);

    @Query("SELECT u FROM OrganizationalUnit u " +
           "LEFT JOIN FETCH u.costCenter " +
           "LEFT JOIN FETCH u.parentUnit " +
           "WHERE u.companyId = :companyId " +
           "ORDER BY u.name ASC")
    List<OrganizationalUnit> findAllByCompanyIdWithDetails(@Param("companyId") UUID companyId);

    @Query("SELECT COUNT(u) FROM OrganizationalUnit u WHERE u.parentUnit.id = :parentUnitId")
    long countByParentUnitId(@Param("parentUnitId") UUID parentUnitId);

    @Query("SELECT u FROM OrganizationalUnit u WHERE u.parentUnit.id = :parentUnitId")
    List<OrganizationalUnit> findByParentUnitId(@Param("parentUnitId") UUID parentUnitId);
}
