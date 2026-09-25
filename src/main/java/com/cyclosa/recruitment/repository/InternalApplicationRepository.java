package com.cyclosa.recruitment.repository;

import com.cyclosa.recruitment.entity.InternalApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InternalApplicationRepository extends JpaRepository<InternalApplication, UUID> {

    List<InternalApplication> findByOpportunityId(UUID opportunityId);

    List<InternalApplication> findByCompanyIdAndEmployeeId(UUID companyId, UUID employeeId);

    Optional<InternalApplication> findByOpportunityIdAndEmployeeId(UUID opportunityId, UUID employeeId);

    Optional<InternalApplication> findByIdAndCompanyId(UUID id, UUID companyId);

    long countByCompanyId(UUID companyId);
}
