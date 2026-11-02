package com.cyclosa.organization.repository;

import com.cyclosa.organization.entity.JobTitle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JobTitleRepository extends JpaRepository<JobTitle, UUID> {

    List<JobTitle> findAllByCompanyId(UUID companyId);

    boolean existsByCompanyIdAndCode(UUID companyId, String code);

    boolean existsByCompanyIdAndCodeAndIdNot(UUID companyId, String code, UUID id);

    @Query("SELECT j FROM JobTitle j WHERE j.companyId = :companyId " +
           "AND (:keyword IS NULL OR LOWER(j.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(j.code) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<JobTitle> search(@Param("companyId") UUID companyId, @Param("keyword") String keyword, Pageable pageable);
}

