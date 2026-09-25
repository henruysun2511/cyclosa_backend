package com.cyclosa.employee.repository;

import com.cyclosa.employee.entity.Employee;
import com.cyclosa.employee.enums.EmploymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, UUID>, JpaSpecificationExecutor<Employee> {

    Optional<Employee> findByIdAndCompanyId(UUID id, UUID companyId);

    Optional<Employee> findByEmployeeCodeAndCompanyId(String employeeCode, UUID companyId);

    List<Employee> findByCompanyId(UUID companyId);

    boolean existsByEmployeeCodeAndCompanyId(String employeeCode, UUID companyId);

    Optional<Employee> findByUserId(UUID userId);

    long countByCompanyIdAndEmploymentStatus(UUID companyId, EmploymentStatus status);

    @Query("SELECT COUNT(e) FROM Employee e WHERE e.companyId = :companyId")
    long countByCompanyId(@Param("companyId") UUID companyId);
}
