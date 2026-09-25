package com.cyclosa.recruitment.repository;

import com.cyclosa.recruitment.entity.JobPosition;
import com.cyclosa.recruitment.enums.JobPositionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface JobPositionRepository extends JpaRepository<JobPosition, UUID>, JpaSpecificationExecutor<JobPosition> {
    Optional<JobPosition> findByIdAndCompanyId(UUID id, UUID companyId);
    Page<JobPosition> findByCompanyId(UUID companyId, Pageable pageable);
    Page<JobPosition> findByCompanyIdAndStatus(UUID companyId, JobPositionStatus status, Pageable pageable);
}
