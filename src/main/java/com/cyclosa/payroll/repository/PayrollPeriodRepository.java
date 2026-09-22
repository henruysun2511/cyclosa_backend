package com.cyclosa.payroll.repository;

import com.cyclosa.payroll.entity.PayrollPeriod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PayrollPeriodRepository extends JpaRepository<PayrollPeriod, UUID>, JpaSpecificationExecutor<PayrollPeriod> {

    boolean existsByCodeAndCompanyId(String code, UUID companyId);

    boolean existsByCompanyIdAndMonthAndYear(UUID companyId, Integer month, Integer year);

    Optional<PayrollPeriod> findByIdAndCompanyId(UUID id, UUID companyId);

    Optional<PayrollPeriod> findByCompanyIdAndMonthAndYear(UUID companyId, Integer month, Integer year);
}
