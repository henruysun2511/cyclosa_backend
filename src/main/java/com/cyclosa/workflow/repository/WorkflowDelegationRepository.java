package com.cyclosa.workflow.repository;

import com.cyclosa.workflow.entity.WorkflowDelegation;
import com.cyclosa.workflow.enums.RequestType;
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
public interface WorkflowDelegationRepository extends JpaRepository<WorkflowDelegation, UUID> {

    @Query("SELECT d FROM WorkflowDelegation d WHERE d.delegatorEmployeeId = :delegatorId " +
           "AND d.isActive = true " +
           "AND :currentDate BETWEEN d.startDate AND d.endDate " +
           "AND (d.requestType IS NULL OR d.requestType = :requestType)")
    Optional<WorkflowDelegation> findActiveDelegation(
            @Param("delegatorId") UUID delegatorId,
            @Param("requestType") RequestType requestType,
            @Param("currentDate") LocalDate currentDate
    );

    Page<WorkflowDelegation> findByDelegatorEmployeeIdOrderByCreatedAtDesc(UUID delegatorId, Pageable pageable);

    Page<WorkflowDelegation> findByDelegateeEmployeeIdOrderByCreatedAtDesc(UUID delegateeId, Pageable pageable);
}

