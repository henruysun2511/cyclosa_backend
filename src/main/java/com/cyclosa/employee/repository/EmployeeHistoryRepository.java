package com.cyclosa.employee.repository;

import com.cyclosa.employee.entity.EmployeeHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EmployeeHistoryRepository extends JpaRepository<EmployeeHistory, UUID> {

    List<EmployeeHistory> findByEmployeeIdOrderByEffectiveDateDescCreatedAtDesc(UUID employeeId);
}
