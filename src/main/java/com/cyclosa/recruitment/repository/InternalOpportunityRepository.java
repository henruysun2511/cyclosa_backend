package com.cyclosa.recruitment.repository;

import com.cyclosa.recruitment.entity.InternalOpportunity;
import com.cyclosa.recruitment.enums.OpportunityStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InternalOpportunityRepository extends JpaRepository<InternalOpportunity, UUID>, JpaSpecificationExecutor<InternalOpportunity> {

    Optional<InternalOpportunity> findByIdAndCompanyId(UUID id, UUID companyId);

    List<InternalOpportunity> findByCompanyIdAndStatus(UUID companyId, OpportunityStatus status);

    long countByCompanyId(UUID companyId);

    long countByCompanyIdAndStatus(UUID companyId, OpportunityStatus status);
}
