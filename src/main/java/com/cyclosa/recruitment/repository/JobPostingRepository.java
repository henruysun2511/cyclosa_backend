package com.cyclosa.recruitment.repository;

import com.cyclosa.recruitment.entity.JobPosting;
import com.cyclosa.recruitment.enums.PostingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JobPostingRepository extends JpaRepository<JobPosting, UUID>, JpaSpecificationExecutor<JobPosting> {
    Optional<JobPosting> findByIdAndCompanyId(UUID id, UUID companyId);
    Page<JobPosting> findByCompanyId(UUID companyId, Pageable pageable);
    Page<JobPosting> findByCompanyIdAndStatus(UUID companyId, PostingStatus status, Pageable pageable);
    List<JobPosting> findByJobPositionId(UUID jobPositionId);
}
