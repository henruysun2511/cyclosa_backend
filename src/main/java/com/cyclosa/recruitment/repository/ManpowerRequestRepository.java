package com.cyclosa.recruitment.repository;

import com.cyclosa.recruitment.entity.ManpowerRequest;
import com.cyclosa.recruitment.enums.ManpowerRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ManpowerRequestRepository extends JpaRepository<ManpowerRequest, UUID>, JpaSpecificationExecutor<ManpowerRequest> {
    Optional<ManpowerRequest> findByIdAndCompanyId(UUID id, UUID companyId);
    Optional<ManpowerRequest> findByWorkflowInstanceId(UUID workflowInstanceId);
    Page<ManpowerRequest> findByCompanyId(UUID companyId, Pageable pageable);
    Page<ManpowerRequest> findByCompanyIdAndStatus(UUID companyId, ManpowerRequestStatus status, Pageable pageable);
    long countByCompanyId(UUID companyId);
}
