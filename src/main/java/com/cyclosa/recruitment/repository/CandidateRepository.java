package com.cyclosa.recruitment.repository;

import com.cyclosa.recruitment.entity.Candidate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CandidateRepository extends JpaRepository<Candidate, UUID>, JpaSpecificationExecutor<Candidate> {
    Optional<Candidate> findByIdAndCompanyId(UUID id, UUID companyId);
    Optional<Candidate> findByCompanyIdAndEmail(UUID companyId, String email);
    boolean existsByCompanyIdAndEmail(UUID companyId, String email);
    Page<Candidate> findByCompanyId(UUID companyId, Pageable pageable);
}
