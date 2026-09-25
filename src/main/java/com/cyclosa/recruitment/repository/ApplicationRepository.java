package com.cyclosa.recruitment.repository;

import com.cyclosa.recruitment.entity.Application;
import com.cyclosa.recruitment.enums.ApplicationStage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, UUID>, JpaSpecificationExecutor<Application> {
    Optional<Application> findByIdAndCompanyId(UUID id, UUID companyId);
    Page<Application> findByCompanyId(UUID companyId, Pageable pageable);
    Page<Application> findByCompanyIdAndJobPositionId(UUID companyId, UUID jobPositionId, Pageable pageable);
    Page<Application> findByCompanyIdAndStage(UUID companyId, ApplicationStage stage, Pageable pageable);
    List<Application> findByCompanyIdAndJobPositionId(UUID companyId, UUID jobPositionId);
    boolean existsByCompanyIdAndCandidateIdAndJobPositionIdAndStageNotIn(
            UUID companyId, UUID candidateId, UUID jobPositionId, List<ApplicationStage> stages
    );
    long countByJobPositionId(UUID jobPositionId);
}
