package com.cyclosa.organization.repository;

import com.cyclosa.organization.entity.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RegionRepository extends JpaRepository<Region, UUID> {

    boolean existsByCodeAndCompanyId(String code, UUID companyId);

    boolean existsByCodeAndCompanyIdAndIdNot(String code, UUID companyId, UUID id);

    @Query("SELECT DISTINCT r FROM Region r " +
           "LEFT JOIN FETCH r.branches b " +
           "WHERE r.companyId = :companyId " +
           "ORDER BY r.name ASC")
    List<Region> findAllByCompanyIdWithBranches(@Param("companyId") UUID companyId);
}
