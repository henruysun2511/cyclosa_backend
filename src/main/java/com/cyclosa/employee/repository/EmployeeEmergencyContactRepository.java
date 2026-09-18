package com.cyclosa.employee.repository;

import com.cyclosa.employee.entity.EmployeeEmergencyContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeEmergencyContactRepository extends JpaRepository<EmployeeEmergencyContact, UUID> {

    List<EmployeeEmergencyContact> findByEmployeeId(UUID employeeId);

    Optional<EmployeeEmergencyContact> findByIdAndEmployeeId(UUID id, UUID employeeId);
}
