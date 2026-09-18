package com.cyclosa.employee.repository;

import com.cyclosa.employee.entity.EmployeePersonalInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeePersonalInfoRepository extends JpaRepository<EmployeePersonalInfo, UUID> {

    Optional<EmployeePersonalInfo> findByEmployeeId(UUID employeeId);

    Optional<EmployeePersonalInfo> findByNationalIdNumber(String nationalIdNumber);

    boolean existsByNationalIdNumber(String nationalIdNumber);
}
