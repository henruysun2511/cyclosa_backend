package com.cyclosa.workflow.service;

import com.cyclosa.common.response.PageData;
import com.cyclosa.workflow.dto.request.*;
import com.cyclosa.workflow.dto.response.WorkflowConditionResponse;
import com.cyclosa.workflow.dto.response.WorkflowDefinitionResponse;
import com.cyclosa.workflow.dto.response.WorkflowStepResponse;
import com.cyclosa.workflow.enums.ApprovalRequestType;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface WorkflowDefinitionService {

    WorkflowDefinitionResponse createWorkflowDefinition(CreateWorkflowDefinitionRequest request, UUID creatorEmployeeId);

    WorkflowDefinitionResponse updateWorkflowDefinition(UUID id, UpdateWorkflowDefinitionRequest request);

    WorkflowDefinitionResponse getWorkflowDefinitionById(UUID id);

    PageData<WorkflowDefinitionResponse> getWorkflowDefinitions(UUID companyId, ApprovalRequestType requestType, Pageable pageable);

    WorkflowStepResponse addStep(UUID definitionId, CreateWorkflowStepRequest request);

    void deleteStep(UUID definitionId, UUID stepId);

    WorkflowConditionResponse addCondition(UUID definitionId, CreateWorkflowConditionRequest request);

    void deleteCondition(UUID definitionId, UUID conditionId);

    WorkflowDefinitionResponse publish(UUID definitionId);

    void deleteWorkflowDefinition(UUID id);
}
