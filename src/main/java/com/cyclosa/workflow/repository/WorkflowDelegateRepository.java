package com.cyclosa.workflow.repository;

import com.cyclosa.workflow.entity.WorkflowDelegate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkflowDelegateRepository extends JpaRepository<WorkflowDelegate, UUID> {

    @Query("SELECT d FROM WorkflowDelegate d " +
           "WHERE d.delegatorEmployeeId = :delegatorId " +
           "AND d.isActive = true " +
           "AND :date BETWEEN d.startDate AND d.endDate")
    Optional<WorkflowDelegate> findActiveDelegation(
            @Param("delegatorId") UUID delegatorId,
            @Param("date") LocalDate date);

    @Query("SELECT d.delegatorEmployeeId FROM WorkflowDelegate d " +
           "WHERE d.delegateEmployeeId = :delegateId " +
           "AND d.isActive = true " +
           "AND :date BETWEEN d.startDate AND d.endDate")
    List<UUID> findDelegatorIdsForDelegate(
            @Param("delegateId") UUID delegateId,
            @Param("date") LocalDate date);

    @Query("SELECT COUNT(d) > 0 FROM WorkflowDelegate d " +
           "WHERE d.delegatorEmployeeId = :delegatorId " +
           "AND d.isActive = true " +
           "AND d.startDate <= :endDate AND d.endDate >= :startDate " +
           "AND (:excludeId IS NULL OR d.id != :excludeId)")
    boolean existsOverlapping(
            @Param("delegatorId") UUID delegatorId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("excludeId") UUID excludeId);

    Page<WorkflowDelegate> findByDelegatorEmployeeId(UUID delegatorEmployeeId, Pageable pageable);

    Page<WorkflowDelegate> findByDelegateEmployeeId(UUID delegateEmployeeId, Pageable pageable);
}
