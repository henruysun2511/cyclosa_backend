package com.cyclosa.recruitment.repository;

import com.cyclosa.recruitment.entity.Interview;
import com.cyclosa.recruitment.enums.InterviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InterviewRepository extends JpaRepository<Interview, UUID>, JpaSpecificationExecutor<Interview> {
    Optional<Interview> findByIdAndCompanyId(UUID id, UUID companyId);
    Page<Interview> findByCompanyId(UUID companyId, Pageable pageable);
    List<Interview> findByApplicationIdOrderByRoundNumberAsc(UUID applicationId);
    Page<Interview> findByCompanyIdAndStatus(UUID companyId, InterviewStatus status, Pageable pageable);
    List<Interview> findByCompanyIdAndScheduledStartTimeBetween(UUID companyId, LocalDateTime start, LocalDateTime end);
}
