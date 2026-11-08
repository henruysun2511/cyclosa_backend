package com.cyclosa.employee.repository;

import com.cyclosa.employee.entity.EmployeeWorkAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeWorkAssignmentRepository extends JpaRepository<EmployeeWorkAssignment, UUID> {

    List<EmployeeWorkAssignment> findAllByEmployeeIdOrderByEffectiveFromDesc(UUID employeeId);

    Optional<EmployeeWorkAssignment> findFirstByEmployeeIdAndEffectiveFromLessThanEqualOrderByEffectiveFromDesc(
            UUID employeeId, LocalDate asOfDate);

    @Query("SELECT wa FROM EmployeeWorkAssignment wa WHERE wa.manager.id = :managerId " +
           "AND wa.effectiveFrom <= :asOfDate " +
           "AND NOT EXISTS (SELECT nextWa FROM EmployeeWorkAssignment nextWa " +
           "                WHERE nextWa.employee.id = wa.employee.id " +
           "                AND nextWa.effectiveFrom <= :asOfDate " +
           "                AND nextWa.effectiveFrom > wa.effectiveFrom)")
    List<EmployeeWorkAssignment> findCurrentDirectReports(
            @Param("managerId") UUID managerId,
            @Param("asOfDate") LocalDate asOfDate);

    @Query("SELECT COUNT(DISTINCT wa.employee.id) FROM EmployeeWorkAssignment wa " +
           "WHERE wa.department.id = :departmentId " +
           "AND wa.effectiveFrom <= :asOfDate " +
           "AND NOT EXISTS (SELECT nextWa FROM EmployeeWorkAssignment nextWa " +
           "                WHERE nextWa.employee.id = wa.employee.id " +
           "                AND nextWa.effectiveFrom <= :asOfDate " +
           "                AND nextWa.effectiveFrom > wa.effectiveFrom)")
    long countCurrentActiveByDepartmentId(
            @Param("departmentId") UUID departmentId,
            @Param("asOfDate") LocalDate asOfDate);

    boolean existsByDepartmentId(UUID departmentId);

    boolean existsByBranchId(UUID branchId);

    boolean existsByJobTitleId(UUID jobTitleId);

    boolean existsByJobLevelId(UUID jobLevelId);
}

