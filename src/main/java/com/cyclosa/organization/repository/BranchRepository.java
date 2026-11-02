package com.cyclosa.organization.repository;

import com.cyclosa.organization.entity.Branch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BranchRepository extends JpaRepository<Branch, UUID> {

    List<Branch> findAllByRegionId(UUID regionId);

    @Query("SELECT b FROM Branch b JOIN Region r ON b.regionId = r.id WHERE r.companyId = :companyId")
    List<Branch> findAllByCompanyId(@Param("companyId") UUID companyId);

    boolean existsByRegionIdAndCode(UUID regionId, String code);

    boolean existsByRegionIdAndCodeAndIdNot(UUID regionId, String code, UUID id);

    @Query("SELECT b FROM Branch b WHERE b.regionId = :regionId " +
           "AND (:keyword IS NULL OR LOWER(b.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(b.code) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:isActive IS NULL OR b.isActive = :isActive)")
    Page<Branch> search(@Param("regionId") UUID regionId,
                        @Param("keyword") String keyword,
                        @Param("isActive") Boolean isActive,
                        Pageable pageable);
}

