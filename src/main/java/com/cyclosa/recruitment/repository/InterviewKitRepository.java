package com.cyclosa.recruitment.repository;

import com.cyclosa.recruitment.entity.InterviewKit;
import com.cyclosa.recruitment.enums.InterviewType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InterviewKitRepository extends JpaRepository<InterviewKit, UUID>, JpaSpecificationExecutor<InterviewKit> {
    Optional<InterviewKit> findByIdAndCompanyId(UUID id, UUID companyId);
    Page<InterviewKit> findByCompanyId(UUID companyId, Pageable pageable);
    List<InterviewKit> findByCompanyIdAndJobPositionId(UUID companyId, UUID jobPositionId);
    Optional<InterviewKit> findByCompanyIdAndJobPositionIdAndInterviewType(UUID companyId, UUID jobPositionId, InterviewType interviewType);
}
