package com.cyclosa.organization.repository;

import com.cyclosa.organization.entity.CostCenter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CostCenterRepository extends JpaRepository<CostCenter, UUID> {

    boolean existsByCodeAndCompanyId(String code, UUID companyId);

    boolean existsByCodeAndCompanyIdAndIdNot(String code, UUID companyId, UUID id);

    List<CostCenter> findByCompanyId(UUID companyId);
}
