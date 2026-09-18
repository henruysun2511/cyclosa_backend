package com.cyclosa.workflow.mapper;

import com.cyclosa.workflow.dto.request.CreateWorkflowConditionRequest;
import com.cyclosa.workflow.dto.request.CreateWorkflowDefinitionRequest;
import com.cyclosa.workflow.dto.request.CreateWorkflowDelegateRequest;
import com.cyclosa.workflow.dto.request.CreateWorkflowStepRequest;
import com.cyclosa.workflow.dto.response.*;
import com.cyclosa.workflow.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface WorkflowMapper {

    @Mapping(target = "version", constant = "1")
    @Mapping(target = "status", constant = "DRAFT")
    @Mapping(target = "publishedAt", ignore = true)
    @Mapping(target = "createdByEmployeeId", ignore = true)
    @Mapping(target = "steps", ignore = true)
    @Mapping(target = "conditions", ignore = true)
    WorkflowDefinition toEntity(CreateWorkflowDefinitionRequest request);

    @Mapping(target = "createdByEmployeeName", ignore = true)
    @Mapping(target = "steps", source = "steps")
    @Mapping(target = "conditions", source = "conditions")
    WorkflowDefinitionResponse toResponse(WorkflowDefinition entity);

    List<WorkflowDefinitionResponse> toResponseList(List<WorkflowDefinition> entities);

    @Mapping(target = "workflowDefinition", ignore = true)
    WorkflowStep toStepEntity(CreateWorkflowStepRequest request);

    @Mapping(target = "workflowDefinitionId", source = "workflowDefinition.id")
    @Mapping(target = "specificApproverName", ignore = true)
    @Mapping(target = "specificRoleName", ignore = true)
    WorkflowStepResponse toStepResponse(WorkflowStep step);

    List<WorkflowStepResponse> toStepResponseList(List<WorkflowStep> steps);

    @Mapping(target = "workflowDefinition", ignore = true)
    WorkflowCondition toConditionEntity(CreateWorkflowConditionRequest request);

    WorkflowConditionResponse toConditionResponse(WorkflowCondition condition);

    List<WorkflowConditionResponse> toConditionResponseList(List<WorkflowCondition> conditions);

    @Mapping(target = "workflowDefinitionName", source = "workflowDefinition.name")
    @Mapping(target = "workflowVersion", source = "workflowDefinition.version")
    @Mapping(target = "requesterEmployeeName", ignore = true)
    @Mapping(target = "currentStepName", ignore = true)
    @Mapping(target = "approvalSteps", source = "approvalSteps")
    WorkflowInstanceResponse toInstanceResponse(WorkflowInstance instance);

    @Mapping(target = "assignedApproverName", ignore = true)
    @Mapping(target = "approverName", ignore = true)
    WorkflowApprovalStepResponse toApprovalStepResponse(WorkflowApprovalStep step);

    List<WorkflowApprovalStepResponse> toApprovalStepResponseList(List<WorkflowApprovalStep> steps);

    @Mapping(target = "delegatorEmployeeId", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    WorkflowDelegate toDelegateEntity(CreateWorkflowDelegateRequest request);

    @Mapping(target = "delegatorName", ignore = true)
    @Mapping(target = "delegateName", ignore = true)
    WorkflowDelegateResponse toDelegateResponse(WorkflowDelegate delegate);

    List<WorkflowDelegateResponse> toDelegateResponseList(List<WorkflowDelegate> delegates);
}
