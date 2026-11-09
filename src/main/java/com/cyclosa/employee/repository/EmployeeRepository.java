package com.cyclosa.employee.repository;

import com.cyclosa.employee.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, UUID>, JpaSpecificationExecutor<Employee> {

    Optional<Employee> findByCompanyIdAndEmployeeCode(UUID companyId, String employeeCode);

    Optional<Employee> findByUserId(UUID userId);

    boolean existsByCompanyIdAndEmployeeCode(UUID companyId, String employeeCode);

    boolean existsByPersonalTaxCode(String personalTaxCode);

    boolean existsByPersonalTaxCodeAndIdNot(String personalTaxCode, UUID id);

    boolean existsBySocialInsuranceNumber(String socialInsuranceNumber);

    boolean existsBySocialInsuranceNumberAndIdNot(String socialInsuranceNumber, UUID id);

    boolean existsByUserId(UUID userId);

    @Query("SELECT e FROM Employee e WHERE (:companyId IS NULL OR e.companyId = :companyId) " +
           "AND (:keyword IS NULL OR :keyword = '' " +
           "OR LOWER(e.employeeCode) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(CONCAT(e.lastName, ' ', e.firstName)) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(e.phone) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(e.personalEmail) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Employee> search(@Param("companyId") UUID companyId, @Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT DISTINCT e FROM Employee e " +
           "WHERE (:companyId IS NULL OR e.companyId = :companyId) " +
           "AND (:keyword IS NULL OR :keyword = '' " +
           "     OR LOWER(e.employeeCode) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "     OR LOWER(CONCAT(e.lastName, ' ', e.firstName)) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "     OR LOWER(e.phone) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "     OR LOWER(e.personalEmail) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:departmentId IS NULL OR EXISTS (" +
           "     SELECT 1 FROM EmployeeWorkAssignment a " +
           "     WHERE a.employee.id = e.id " +
           "     AND a.department.id = :departmentId " +
           "     AND a.effectiveFrom <= :asOfDate " +
           "     AND NOT EXISTS (" +
           "         SELECT 1 FROM EmployeeWorkAssignment a2 " +
           "         WHERE a2.employee.id = e.id " +
           "         AND a2.effectiveFrom <= :asOfDate " +
           "         AND a2.effectiveFrom > a.effectiveFrom" +
           "     )" +
           ")) " +
           "AND (:status IS NULL OR EXISTS (" +
           "     SELECT 1 FROM EmployeeStatusHistory s " +
           "     WHERE s.employee.id = e.id " +
           "     AND s.status = :status " +
           "     AND s.effectiveFrom <= :asOfDate " +
           "     AND NOT EXISTS (" +
           "         SELECT 1 FROM EmployeeStatusHistory s2 " +
           "         WHERE s2.employee.id = e.id " +
           "         AND s2.effectiveFrom <= :asOfDate " +
           "         AND s2.effectiveFrom > s.effectiveFrom" +
           "     )" +
           "))")
    Page<Employee> searchWithFilters(
            @Param("companyId") UUID companyId,
            @Param("departmentId") UUID departmentId,
            @Param("status") com.cyclosa.employee.entity.enums.EmployeeStatus status,
            @Param("keyword") String keyword,
            @Param("asOfDate") java.time.LocalDate asOfDate,
            Pageable pageable
    );

    @Query("SELECT COUNT(e) FROM Employee e WHERE e.companyId = :companyId AND e.employeeCode LIKE CONCAT(:prefix, '%')")
    long countByPrefix(@Param("companyId") UUID companyId, @Param("prefix") String prefix);
}

