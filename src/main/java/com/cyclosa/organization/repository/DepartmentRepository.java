package com.cyclosa.organization.repository;

import com.cyclosa.organization.entity.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, UUID> {

    List<Department> findAllByCompanyId(UUID companyId);

    @Query("SELECT d FROM Department d WHERE d.companyId = :companyId AND (d.dissolvedDate IS NULL OR d.dissolvedDate > :asOfDate)")
    List<Department> findAllActiveByCompanyId(@Param("companyId") UUID companyId, @Param("asOfDate") LocalDate asOfDate);

    boolean existsByCompanyIdAndCode(UUID companyId, String code);

    boolean existsByCompanyIdAndCodeAndIdNot(UUID companyId, String code, UUID id);

    @Query("SELECT d FROM Department d JOIN d.versions v WHERE d.companyId = :companyId " +
           "AND (:keyword IS NULL OR LOWER(d.code) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(v.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "GROUP BY d")
    Page<Department> search(@Param("companyId") UUID companyId, @Param("keyword") String keyword, Pageable pageable);
}

