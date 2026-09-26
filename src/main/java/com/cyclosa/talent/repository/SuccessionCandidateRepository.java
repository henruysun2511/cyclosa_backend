package com.cyclosa.talent.repository;

import com.cyclosa.talent.entity.SuccessionCandidate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SuccessionCandidateRepository extends JpaRepository<SuccessionCandidate, UUID>, JpaSpecificationExecutor<SuccessionCandidate> {

    boolean existsBySuccessionPlanIdAndEmployeeId(UUID successionPlanId, UUID employeeId);

    Optional<SuccessionCandidate> findBySuccessionPlanIdAndId(UUID successionPlanId, UUID id);
}
