package com.cyclosa.performance.repository;

import com.cyclosa.performance.entity.PerformanceEvaluation;
import com.cyclosa.performance.enums.EvaluationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PerformanceEvaluationRepository extends JpaRepository<PerformanceEvaluation, UUID> {

    Optional<PerformanceEvaluation> findByEmployeeIdAndPerformanceCycleId(UUID employeeId, UUID performanceCycleId);

    @Query("SELECT e FROM PerformanceEvaluation e WHERE " +
           "(:companyId IS NULL OR e.companyId = :companyId) AND " +
           "(:cycleId IS NULL OR e.performanceCycleId = :cycleId) AND " +
           "(:employeeId IS NULL OR e.employeeId = :employeeId) AND " +
           "(:employeeIds IS NULL OR e.employeeId IN :employeeIds) AND " +
           "(:status IS NULL OR e.status = :status)")
    Page<PerformanceEvaluation> searchEvaluations(
            @Param("companyId") UUID companyId,
            @Param("cycleId") UUID cycleId,
            @Param("employeeId") UUID employeeId,
            @Param("employeeIds") Collection<UUID> employeeIds,
            @Param("status") EvaluationStatus status,
            Pageable pageable
    );
}
