package com.cyclosa.workflow.repository;

import com.cyclosa.workflow.entity.WorkflowStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkflowStepRepository extends JpaRepository<WorkflowStep, UUID> {

    List<WorkflowStep> findByWorkflowDefinitionIdOrderByStepOrderAsc(UUID workflowDefinitionId);

    Optional<WorkflowStep> findByWorkflowDefinitionIdAndStepOrder(UUID workflowDefinitionId, Integer stepOrder);

    void deleteByWorkflowDefinitionId(UUID workflowDefinitionId);
}
