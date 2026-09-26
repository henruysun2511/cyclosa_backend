package com.cyclosa.workplace.repository;

import com.cyclosa.workplace.entity.WorkplaceSite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkplaceSiteRepository extends JpaRepository<WorkplaceSite, UUID> {

    Optional<WorkplaceSite> findByIdAndCompanyId(UUID id, UUID companyId);

    boolean existsByCompanyIdAndCode(UUID companyId, String code);

    boolean existsByCompanyIdAndCodeAndIdNot(UUID companyId, String code, UUID id);

    List<WorkplaceSite> findByCompanyId(UUID companyId);

    @Query("SELECT s FROM WorkplaceSite s WHERE s.companyId = :companyId " +
           "AND (:search IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(s.code) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<WorkplaceSite> searchSites(@Param("companyId") UUID companyId, @Param("search") String search, Pageable pageable);
}
