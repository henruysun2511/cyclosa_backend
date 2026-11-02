package com.cyclosa.organization.repository;

import com.cyclosa.organization.entity.CostCenter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CostCenterRepository extends JpaRepository<CostCenter, UUID> {

    List<CostCenter> findAllByCompanyId(UUID companyId);

    boolean existsByCompanyIdAndCode(UUID companyId, String code);

    boolean existsByCompanyIdAndCodeAndIdNot(UUID companyId, String code, UUID id);

    @Query("SELECT c FROM CostCenter c WHERE c.companyId = :companyId " +
           "AND (:keyword IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(c.code) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<CostCenter> search(@Param("companyId") UUID companyId, @Param("keyword") String keyword, Pageable pageable);
}

