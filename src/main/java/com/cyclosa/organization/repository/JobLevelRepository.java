package com.cyclosa.organization.repository;

import com.cyclosa.organization.entity.JobLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JobLevelRepository extends JpaRepository<JobLevel, UUID> {

    List<JobLevel> findAllByCompanyIdOrderByRankOrderAsc(UUID companyId);

    boolean existsByCompanyIdAndCode(UUID companyId, String code);

    boolean existsByCompanyIdAndCodeAndIdNot(UUID companyId, String code, UUID id);

    boolean existsByCompanyIdAndRankOrder(UUID companyId, Integer rankOrder);

    boolean existsByCompanyIdAndRankOrderAndIdNot(UUID companyId, Integer rankOrder, UUID id);

    @Query("SELECT l FROM JobLevel l WHERE l.companyId = :companyId " +
           "AND (:keyword IS NULL OR LOWER(l.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(l.code) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<JobLevel> search(@Param("companyId") UUID companyId, @Param("keyword") String keyword, Pageable pageable);
}

