package com.cyclosa.recruitment.repository;

import com.cyclosa.recruitment.entity.TalentPool;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TalentPoolRepository extends JpaRepository<TalentPool, UUID>, JpaSpecificationExecutor<TalentPool> {
    Optional<TalentPool> findByIdAndCompanyId(UUID id, UUID companyId);
    Page<TalentPool> findByCompanyId(UUID companyId, Pageable pageable);
    boolean existsByCompanyIdAndCandidateId(UUID companyId, UUID candidateId);
    Optional<TalentPool> findByCompanyIdAndCandidateId(UUID companyId, UUID candidateId);
}
