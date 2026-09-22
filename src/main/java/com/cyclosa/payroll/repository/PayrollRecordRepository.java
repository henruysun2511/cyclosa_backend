package com.cyclosa.payroll.repository;

import com.cyclosa.payroll.entity.PayrollRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PayrollRecordRepository extends JpaRepository<PayrollRecord, UUID>, JpaSpecificationExecutor<PayrollRecord> {

    Optional<PayrollRecord> findByPayrollPeriodIdAndEmployeeId(UUID payrollPeriodId, UUID employeeId);

    Optional<PayrollRecord> findByIdAndCompanyId(UUID id, UUID companyId);

    @Query("SELECT pr FROM PayrollRecord pr LEFT JOIN FETCH pr.items WHERE pr.id = :id AND pr.companyId = :companyId")
    Optional<PayrollRecord> findByIdAndCompanyIdWithItems(@Param("id") UUID id, @Param("companyId") UUID companyId);

    @Query("SELECT pr FROM PayrollRecord pr LEFT JOIN FETCH pr.items WHERE pr.id = :id")
    Optional<PayrollRecord> findByIdWithItems(@Param("id") UUID id);

    List<PayrollRecord> findAllByPayrollPeriodId(UUID payrollPeriodId);

    List<PayrollRecord> findAllByEmployeeIdOrderByCreatedAtDesc(UUID employeeId);

    void deleteAllByPayrollPeriodId(UUID payrollPeriodId);
}
