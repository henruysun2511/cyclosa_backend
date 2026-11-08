package com.cyclosa.employee.repository;

import com.cyclosa.employee.entity.EmployeeBankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeBankAccountRepository extends JpaRepository<EmployeeBankAccount, UUID> {

    Optional<EmployeeBankAccount> findByEmployeeId(UUID employeeId);

    boolean existsByEmployeeId(UUID employeeId);
}

