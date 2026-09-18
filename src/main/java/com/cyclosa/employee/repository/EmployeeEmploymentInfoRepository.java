package com.cyclosa.employee.repository;

import com.cyclosa.employee.entity.EmployeeEmploymentInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeEmploymentInfoRepository extends JpaRepository<EmployeeEmploymentInfo, UUID> {

    Optional<EmployeeEmploymentInfo> findByEmployeeId(UUID employeeId);

    List<EmployeeEmploymentInfo> findByOrganizationalUnitId(UUID organizationalUnitId);

    List<EmployeeEmploymentInfo> findByManagerEmployeeId(UUID managerEmployeeId);

    List<EmployeeEmploymentInfo> findByBranchId(UUID branchId);
}
