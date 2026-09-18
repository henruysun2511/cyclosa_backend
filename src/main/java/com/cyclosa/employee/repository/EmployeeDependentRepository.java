package com.cyclosa.employee.repository;

import com.cyclosa.employee.entity.EmployeeDependent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeDependentRepository extends JpaRepository<EmployeeDependent, UUID> {

    List<EmployeeDependent> findByEmployeeId(UUID employeeId);

    Optional<EmployeeDependent> findByIdAndEmployeeId(UUID id, UUID employeeId);
}
