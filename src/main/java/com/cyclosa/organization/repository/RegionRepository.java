package com.cyclosa.organization.repository;

import com.cyclosa.organization.entity.Region;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RegionRepository extends JpaRepository<Region, UUID> {

    List<Region> findAllByCompanyId(UUID companyId);

    boolean existsByCompanyIdAndCode(UUID companyId, String code);

    boolean existsByCompanyIdAndCodeAndIdNot(UUID companyId, String code, UUID id);

    @Query("SELECT r FROM Region r WHERE r.companyId = :companyId " +
           "AND (:keyword IS NULL OR LOWER(r.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(r.code) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Region> search(@Param("companyId") UUID companyId, @Param("keyword") String keyword, Pageable pageable);
}

