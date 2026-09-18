package com.cyclosa.workflow.repository;

import com.cyclosa.workflow.entity.WorkflowInstance;
import com.cyclosa.workflow.enums.ApprovalRequestType;
import com.cyclosa.workflow.enums.ApprovalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkflowInstanceRepository extends JpaRepository<WorkflowInstance, UUID> {

    Optional<WorkflowInstance> findByRequestTypeAndRequestId(ApprovalRequestType requestType, UUID requestId);

    List<WorkflowInstance> findAllByRequestTypeAndRequestId(ApprovalRequestType requestType, UUID requestId);

    Page<WorkflowInstance> findByCompanyIdAndStatus(UUID companyId, ApprovalStatus status, Pageable pageable);

    Page<WorkflowInstance> findByRequesterEmployeeId(UUID requesterEmployeeId, Pageable pageable);
}
