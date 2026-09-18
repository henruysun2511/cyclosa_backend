package com.cyclosa.workflow.repository;

import com.cyclosa.workflow.entity.WorkflowApprovalStep;
import com.cyclosa.workflow.enums.WorkflowAction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkflowApprovalStepRepository extends JpaRepository<WorkflowApprovalStep, UUID> {

    List<WorkflowApprovalStep> findByWorkflowInstanceIdOrderByStepOrderAsc(UUID workflowInstanceId);

    Optional<WorkflowApprovalStep> findByWorkflowInstanceIdAndStepId(UUID workflowInstanceId, UUID stepId);

    Optional<WorkflowApprovalStep> findByWorkflowInstanceIdAndAction(UUID workflowInstanceId, WorkflowAction action);

    @Query("SELECT s FROM WorkflowApprovalStep s " +
           "JOIN FETCH s.workflowInstance wi " +
           "WHERE s.action = :action " +
           "AND s.assignedApproverEmployeeId IN :approverIds " +
           "ORDER BY s.createdAt DESC")
    Page<WorkflowApprovalStep> findPendingStepsForApprovers(
            @Param("approverIds") Collection<UUID> approverIds,
            @Param("action") WorkflowAction action,
            Pageable pageable);

    @Query("SELECT s FROM WorkflowApprovalStep s " +
           "JOIN FETCH s.workflowInstance wi " +
           "WHERE s.action = 'PENDING' " +
           "AND s.slaDeadlineAt IS NOT NULL " +
           "AND s.slaDeadlineAt < :now")
    List<WorkflowApprovalStep> findOverduePendingSteps(@Param("now") LocalDateTime now);
}
