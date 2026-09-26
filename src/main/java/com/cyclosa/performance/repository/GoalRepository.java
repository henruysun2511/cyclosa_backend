package com.cyclosa.performance.repository;

import com.cyclosa.performance.entity.Goal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface GoalRepository extends JpaRepository<Goal, UUID> {

    List<Goal> findByEmployeeIdAndPerformanceCycleId(UUID employeeId, UUID performanceCycleId);

    @Query("SELECT COALESCE(SUM(g.weightPercentage), 0) FROM Goal g WHERE g.employeeId = :employeeId AND g.performanceCycleId = :cycleId AND (:excludeGoalId IS NULL OR g.id <> :excludeGoalId)")
    BigDecimal sumWeightByEmployeeAndCycle(
            @Param("employeeId") UUID employeeId,
            @Param("cycleId") UUID cycleId,
            @Param("excludeGoalId") UUID excludeGoalId
    );

    @Query("SELECT g FROM Goal g WHERE " +
           "(:employeeId IS NULL OR g.employeeId = :employeeId) AND " +
           "(:cycleId IS NULL OR g.performanceCycleId = :cycleId)")
    Page<Goal> searchGoals(
            @Param("employeeId") UUID employeeId,
            @Param("cycleId") UUID cycleId,
            Pageable pageable
    );
}
