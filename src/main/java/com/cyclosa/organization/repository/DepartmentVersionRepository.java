package com.cyclosa.organization.repository;

import com.cyclosa.organization.entity.DepartmentVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DepartmentVersionRepository extends JpaRepository<DepartmentVersion, UUID> {

    List<DepartmentVersion> findAllByDepartmentIdOrderByEffectiveFromDesc(UUID departmentId);

    @Query("SELECT v FROM DepartmentVersion v WHERE v.department.id = :departmentId " +
           "AND v.effectiveFrom <= :asOfDate ORDER BY v.effectiveFrom DESC LIMIT 1")
    Optional<DepartmentVersion> findActiveVersionAt(@Param("departmentId") UUID departmentId, @Param("asOfDate") LocalDate asOfDate);

    @Query("SELECT v FROM DepartmentVersion v WHERE v.department.id = :departmentId " +
           "ORDER BY v.effectiveFrom DESC LIMIT 1")
    Optional<DepartmentVersion> findLatestVersion(@Param("departmentId") UUID departmentId);

    @Query(value = "SELECT DISTINCT ON (dv.department_id) dv.* FROM department_versions dv " +
                   "JOIN departments d ON dv.department_id = d.id " +
                   "WHERE d.company_id = :companyId " +
                   "AND (d.dissolved_date IS NULL OR d.dissolved_date > :asOfDate) " +
                   "AND dv.effective_from <= :asOfDate " +
                   "AND dv.deleted_at IS NULL AND d.deleted_at IS NULL " +
                   "ORDER BY dv.department_id, dv.effective_from DESC",
           nativeQuery = true)
    List<DepartmentVersion> findActiveVersionsForCompanyAt(@Param("companyId") UUID companyId, @Param("asOfDate") LocalDate asOfDate);

    @Query(value = "SELECT DISTINCT ON (dv.department_id) dv.* FROM department_versions dv " +
                   "JOIN departments d ON dv.department_id = d.id " +
                   "WHERE d.company_id = :companyId " +
                   "AND dv.deleted_at IS NULL AND d.deleted_at IS NULL " +
                   "ORDER BY dv.department_id, dv.effective_from DESC",
           nativeQuery = true)
    List<DepartmentVersion> findLatestVersionsForCompany(@Param("companyId") UUID companyId);

    @Query("SELECT COUNT(v) > 0 FROM DepartmentVersion v WHERE v.parentDepartmentId = :parentId")
    boolean existsByParentDepartmentId(@Param("parentId") UUID parentId);
}
