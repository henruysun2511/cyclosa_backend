package com.cyclosa.workflow.repository;

import com.cyclosa.workflow.entity.WorkflowDefinition;
import com.cyclosa.workflow.enums.ApprovalRequestType;
import com.cyclosa.workflow.enums.WorkflowStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkflowDefinitionRepository extends JpaRepository<WorkflowDefinition, UUID> {

    Optional<WorkflowDefinition> findByCompanyIdAndRequestTypeAndStatus(
            UUID companyId, ApprovalRequestType requestType, WorkflowStatus status);

    Page<WorkflowDefinition> findByCompanyId(UUID companyId, Pageable pageable);

    Page<WorkflowDefinition> findByCompanyIdAndRequestType(
            UUID companyId, ApprovalRequestType requestType, Pageable pageable);

    @Query("SELECT COALESCE(MAX(w.version), 0) FROM WorkflowDefinition w " +
           "WHERE w.companyId = :companyId AND w.requestType = :requestType")
    int findMaxVersionByCompanyIdAndRequestType(
            @Param("companyId") UUID companyId,
            @Param("requestType") ApprovalRequestType requestType);
}
