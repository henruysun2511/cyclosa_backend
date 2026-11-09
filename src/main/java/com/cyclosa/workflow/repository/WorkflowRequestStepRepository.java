package com.cyclosa.workflow.repository;

import com.cyclosa.workflow.entity.WorkflowRequestStep;
import com.cyclosa.workflow.enums.RequestType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkflowRequestStepRepository extends JpaRepository<WorkflowRequestStep, UUID> {

    List<WorkflowRequestStep> findByWorkflowRequestIdOrderByStepOrderAsc(UUID requestId);

    Optional<WorkflowRequestStep> findByWorkflowRequestIdAndIsCurrentTrue(UUID requestId);

    @Query("SELECT s FROM WorkflowRequestStep s JOIN s.workflowRequest r " +
           "WHERE s.assigneeEmployeeId = :employeeId " +
           "AND s.isCurrent = true " +
           "AND r.cancelledAt IS NULL " +
           "AND (:requestType IS NULL OR r.requestType = :requestType) " +
           "AND (:keyword IS NULL OR :keyword = '' OR LOWER(r.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(r.referenceCode) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<WorkflowRequestStep> findInboxByAssignee(
            @Param("employeeId") UUID employeeId,
            @Param("requestType") RequestType requestType,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    @Query("SELECT s FROM WorkflowRequestStep s JOIN s.workflowRequest r " +
           "WHERE s.assigneeEmployeeId = :employeeId " +
           "AND s.decision IS NOT NULL " +
           "AND (:requestType IS NULL OR r.requestType = :requestType) " +
           "AND (:keyword IS NULL OR :keyword = '' OR LOWER(r.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(r.referenceCode) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<WorkflowRequestStep> findHistoryByAssignee(
            @Param("employeeId") UUID employeeId,
            @Param("requestType") RequestType requestType,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    long countByAssigneeEmployeeIdAndIsCurrentTrue(UUID employeeId);

    @Query("SELECT COUNT(s) FROM WorkflowRequestStep s " +
           "WHERE s.assigneeEmployeeId = :employeeId AND s.decision = 'APPROVED'")
    long countApprovedByEmployee(@Param("employeeId") UUID employeeId);
}

