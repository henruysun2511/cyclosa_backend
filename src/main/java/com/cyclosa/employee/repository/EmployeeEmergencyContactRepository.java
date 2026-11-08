package com.cyclosa.employee.repository;

import com.cyclosa.employee.entity.EmployeeEmergencyContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EmployeeEmergencyContactRepository extends JpaRepository<EmployeeEmergencyContact, UUID> {

    List<EmployeeEmergencyContact> findAllByEmployeeId(UUID employeeId);
}

