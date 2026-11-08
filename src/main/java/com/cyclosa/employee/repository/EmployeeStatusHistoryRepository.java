package com.cyclosa.employee.repository;

import com.cyclosa.employee.entity.EmployeeStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeStatusHistoryRepository extends JpaRepository<EmployeeStatusHistory, UUID> {

    List<EmployeeStatusHistory> findAllByEmployeeIdOrderByEffectiveFromDesc(UUID employeeId);

    Optional<EmployeeStatusHistory> findFirstByEmployeeIdAndEffectiveFromLessThanEqualOrderByEffectiveFromDesc(
            UUID employeeId, LocalDate asOfDate);
}

