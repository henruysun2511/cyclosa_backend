package com.cyclosa.payroll.repository;

import com.cyclosa.payroll.entity.SalaryComponent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SalaryComponentRepository extends JpaRepository<SalaryComponent, UUID>, JpaSpecificationExecutor<SalaryComponent> {

    boolean existsByCodeAndCompanyId(String code, UUID companyId);

    Optional<SalaryComponent> findByIdAndCompanyId(UUID id, UUID companyId);

    List<SalaryComponent> findAllByCompanyId(UUID companyId);
}
