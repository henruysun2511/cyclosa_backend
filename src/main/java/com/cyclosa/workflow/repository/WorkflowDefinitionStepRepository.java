package com.cyclosa.workflow.repository;

import com.cyclosa.workflow.entity.WorkflowDefinitionStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WorkflowDefinitionStepRepository extends JpaRepository<WorkflowDefinitionStep, UUID> {

    List<WorkflowDefinitionStep> findByWorkflowDefinitionIdOrderByStepOrderAsc(UUID definitionId);

    void deleteByWorkflowDefinitionId(UUID definitionId);
}

