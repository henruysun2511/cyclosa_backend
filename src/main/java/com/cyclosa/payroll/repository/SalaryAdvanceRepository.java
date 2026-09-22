package com.cyclosa.payroll.repository;

import com.cyclosa.payroll.entity.SalaryAdvance;
import com.cyclosa.payroll.enums.SalaryAdvanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SalaryAdvanceRepository extends JpaRepository<SalaryAdvance, UUID>, JpaSpecificationExecutor<SalaryAdvance> {

    Optional<SalaryAdvance> findByIdAndCompanyId(UUID id, UUID companyId);

    List<SalaryAdvance> findAllByCompanyIdAndEmployeeIdAndStatus(UUID companyId, UUID employeeId, SalaryAdvanceStatus status);

    List<SalaryAdvance> findAllByCompanyIdAndEmployeeIdAndStatusAndRequestDateBetween(
            UUID companyId, UUID employeeId, SalaryAdvanceStatus status, LocalDate fromDate, LocalDate toDate);

    List<SalaryAdvance> findAllByEmployeeIdOrderByRequestDateDesc(UUID employeeId);
}
