package com.cyclosa.talent.repository;

import com.cyclosa.talent.entity.SuccessionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SuccessionPlanRepository extends JpaRepository<SuccessionPlan, UUID>, JpaSpecificationExecutor<SuccessionPlan> {

    boolean existsByCompanyIdAndPositionId(UUID companyId, UUID positionId);

    Optional<SuccessionPlan> findByIdAndCompanyId(UUID id, UUID companyId);

    Optional<SuccessionPlan> findByCompanyIdAndPositionId(UUID companyId, UUID positionId);

    @Query("SELECT p FROM SuccessionPlan p LEFT JOIN FETCH p.candidates WHERE p.id = :id AND p.companyId = :companyId")
    Optional<SuccessionPlan> findByIdWithCandidates(@Param("id") UUID id, @Param("companyId") UUID companyId);
}
