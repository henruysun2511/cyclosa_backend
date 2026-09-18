package com.cyclosa.workflow.repository;

import com.cyclosa.workflow.entity.WorkflowCondition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WorkflowConditionRepository extends JpaRepository<WorkflowCondition, UUID> {

    List<WorkflowCondition> findByWorkflowDefinitionIdOrderByPriorityAsc(UUID workflowDefinitionId);

    List<WorkflowCondition> findByWorkflowDefinitionIdAndFromStepIdOrderByPriorityAsc(
            UUID workflowDefinitionId, UUID fromStepId);

    List<WorkflowCondition> findByWorkflowDefinitionIdAndFromStepIdIsNullOrderByPriorityAsc(
            UUID workflowDefinitionId);

    void deleteByWorkflowDefinitionId(UUID workflowDefinitionId);
}
