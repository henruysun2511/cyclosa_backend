package com.cyclosa.workflow.service;

import com.cyclosa.common.exception.AppException;
import com.cyclosa.common.response.PageData;
import com.cyclosa.employee.entity.Employee;
import com.cyclosa.employee.repository.EmployeeRepository;
import com.cyclosa.role.entity.Role;
import com.cyclosa.role.repository.RoleRepository;
import com.cyclosa.workflow.dto.request.*;
import com.cyclosa.workflow.dto.response.WorkflowConditionResponse;
import com.cyclosa.workflow.dto.response.WorkflowDefinitionResponse;
import com.cyclosa.workflow.dto.response.WorkflowStepResponse;
import com.cyclosa.workflow.entity.WorkflowCondition;
import com.cyclosa.workflow.entity.WorkflowDefinition;
import com.cyclosa.workflow.entity.WorkflowStep;
import com.cyclosa.workflow.enums.ApprovalRequestType;
import com.cyclosa.workflow.enums.WorkflowStatus;
import com.cyclosa.workflow.exception.WorkflowErrorCode;
import com.cyclosa.workflow.mapper.WorkflowMapper;
import com.cyclosa.workflow.repository.WorkflowConditionRepository;
import com.cyclosa.workflow.repository.WorkflowDefinitionRepository;
import com.cyclosa.workflow.repository.WorkflowStepRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowDefinitionService {

    private final WorkflowDefinitionRepository definitionRepository;
    private final WorkflowStepRepository stepRepository;
    private final WorkflowConditionRepository conditionRepository;
    private final EmployeeRepository employeeRepository;
    private final RoleRepository roleRepository;
    private final WorkflowMapper workflowMapper;
    @Transactional
    public WorkflowDefinitionResponse createWorkflowDefinition(CreateWorkflowDefinitionRequest request, UUID creatorEmployeeId) {
        WorkflowDefinition entity = workflowMapper.toEntity(request);
        entity.setCreatedByEmployeeId(creatorEmployeeId);
        entity.setStatus(WorkflowStatus.DRAFT);
        entity.setVersion(1);

        WorkflowDefinition saved = definitionRepository.save(entity);
        return enrichDefinitionResponse(saved);
    }
    @Transactional
    public WorkflowDefinitionResponse updateWorkflowDefinition(UUID id, UpdateWorkflowDefinitionRequest request) {
        WorkflowDefinition definition = getEntityOrThrow(id);

        if (definition.getStatus() == WorkflowStatus.PUBLISHED) {
            throw new AppException(WorkflowErrorCode.CANNOT_MODIFY_PUBLISHED_WORKFLOW);
        }

        definition.setName(request.getName());
        definition.setDescription(request.getDescription());
        WorkflowDefinition saved = definitionRepository.save(definition);
        return enrichDefinitionResponse(saved);
    }
    @Transactional(readOnly = true)
    public WorkflowDefinitionResponse getWorkflowDefinitionById(UUID id) {
        WorkflowDefinition definition = getEntityOrThrow(id);
        return enrichDefinitionResponse(definition);
    }
    @Transactional(readOnly = true)
    public PageData<WorkflowDefinitionResponse> getWorkflowDefinitions(
            UUID companyId, ApprovalRequestType requestType, Pageable pageable) {

        Page<WorkflowDefinition> page;
        if (requestType != null) {
            page = definitionRepository.findByCompanyIdAndRequestType(companyId, requestType, pageable);
        } else {
            page = definitionRepository.findByCompanyId(companyId, pageable);
        }

        List<WorkflowDefinitionResponse> responses = page.getContent().stream()
                .map(this::enrichDefinitionResponse)
                .toList();

        return PageData.of(page, responses);
    }
    @Transactional
    public WorkflowStepResponse addStep(UUID definitionId, CreateWorkflowStepRequest request) {
        WorkflowDefinition definition = getEntityOrThrow(definitionId);
        if (definition.getStatus() == WorkflowStatus.PUBLISHED) {
            throw new AppException(WorkflowErrorCode.CANNOT_MODIFY_PUBLISHED_WORKFLOW);
        }

        WorkflowStep step = workflowMapper.toStepEntity(request);
        step.setWorkflowDefinition(definition);

        WorkflowStep saved = stepRepository.save(step);
        return enrichStepResponse(saved);
    }
    @Transactional
    public void deleteStep(UUID definitionId, UUID stepId) {
        WorkflowDefinition definition = getEntityOrThrow(definitionId);
        if (definition.getStatus() == WorkflowStatus.PUBLISHED) {
            throw new AppException(WorkflowErrorCode.CANNOT_MODIFY_PUBLISHED_WORKFLOW);
        }

        WorkflowStep step = stepRepository.findById(stepId)
                .orElseThrow(() -> new AppException(WorkflowErrorCode.WORKFLOW_STEP_NOT_FOUND));

        if (!step.getWorkflowDefinition().getId().equals(definitionId)) {
            throw new AppException(WorkflowErrorCode.WORKFLOW_STEP_NOT_FOUND);
        }

        stepRepository.delete(step);
    }
    @Transactional
    public WorkflowConditionResponse addCondition(UUID definitionId, CreateWorkflowConditionRequest request) {
        WorkflowDefinition definition = getEntityOrThrow(definitionId);
        if (definition.getStatus() == WorkflowStatus.PUBLISHED) {
            throw new AppException(WorkflowErrorCode.CANNOT_MODIFY_PUBLISHED_WORKFLOW);
        }

        WorkflowCondition condition = workflowMapper.toConditionEntity(request);
        condition.setWorkflowDefinition(definition);

        WorkflowCondition saved = conditionRepository.save(condition);
        return workflowMapper.toConditionResponse(saved);
    }
    @Transactional
    public void deleteCondition(UUID definitionId, UUID conditionId) {
        WorkflowDefinition definition = getEntityOrThrow(definitionId);
        if (definition.getStatus() == WorkflowStatus.PUBLISHED) {
            throw new AppException(WorkflowErrorCode.CANNOT_MODIFY_PUBLISHED_WORKFLOW);
        }

        WorkflowCondition condition = conditionRepository.findById(conditionId)
                .orElseThrow(() -> new AppException(WorkflowErrorCode.INVALID_CONDITION_EXPRESSION, "Không tìm thấy điều kiện"));

        if (!condition.getWorkflowDefinition().getId().equals(definitionId)) {
            throw new AppException(WorkflowErrorCode.INVALID_CONDITION_EXPRESSION, "Điều kiện không thuộc quy trình này");
        }

        conditionRepository.delete(condition);
    }
    @Transactional
    public WorkflowDefinitionResponse publish(UUID definitionId) {
        WorkflowDefinition definition = getEntityOrThrow(definitionId);

        List<WorkflowStep> steps = stepRepository.findByWorkflowDefinitionIdOrderByStepOrderAsc(definitionId);
        if (steps.isEmpty()) {
            throw new AppException(WorkflowErrorCode.NO_PUBLISHED_WORKFLOW,
                    "Không thể xuất bản quy trình không có bước phê duyệt nào");
        }

        // Archive current published workflow if any
        Optional<WorkflowDefinition> currentPublishedOpt = definitionRepository.findByCompanyIdAndRequestTypeAndStatus(
                definition.getCompanyId(), definition.getRequestType(), WorkflowStatus.PUBLISHED);

        currentPublishedOpt.ifPresent(current -> {
            if (!current.getId().equals(definition.getId())) {
                current.setStatus(WorkflowStatus.ARCHIVED);
                definitionRepository.save(current);
                log.info("Lưu trữ bản quy trình cũ ID: {} Version: {}", current.getId(), current.getVersion());
            }
        });

        int maxVersion = definitionRepository.findMaxVersionByCompanyIdAndRequestType(
                definition.getCompanyId(), definition.getRequestType());

        definition.setVersion(maxVersion + 1);
        definition.setStatus(WorkflowStatus.PUBLISHED);
        definition.setPublishedAt(LocalDateTime.now());

        WorkflowDefinition saved = definitionRepository.save(definition);
        log.info("Đã xuất bản Workflow Definition ID: {} RequestType: {} Version: {}",
                saved.getId(), saved.getRequestType(), saved.getVersion());

        return enrichDefinitionResponse(saved);
    }
    @Transactional
    public void deleteWorkflowDefinition(UUID id) {
        WorkflowDefinition definition = getEntityOrThrow(id);
        if (definition.getStatus() == WorkflowStatus.PUBLISHED) {
            throw new AppException(WorkflowErrorCode.CANNOT_MODIFY_PUBLISHED_WORKFLOW,
                    "Không thể xóa quy trình đang được kích hoạt. Hãy lưu trữ nó.");
        }
        definitionRepository.delete(definition);
    }

    private WorkflowDefinition getEntityOrThrow(UUID id) {
        return definitionRepository.findById(id)
                .orElseThrow(() -> new AppException(WorkflowErrorCode.WORKFLOW_DEFINITION_NOT_FOUND));
    }

    private WorkflowDefinitionResponse enrichDefinitionResponse(WorkflowDefinition definition) {
        WorkflowDefinitionResponse response = workflowMapper.toResponse(definition);

        if (definition.getCreatedByEmployeeId() != null) {
            employeeRepository.findById(definition.getCreatedByEmployeeId())
                    .ifPresent(emp -> response.setCreatedByEmployeeName(emp.getFullName()));
        }

        List<WorkflowStep> steps = stepRepository.findByWorkflowDefinitionIdOrderByStepOrderAsc(definition.getId());
        response.setSteps(steps.stream().map(this::enrichStepResponse).toList());

        List<WorkflowCondition> conditions = conditionRepository.findByWorkflowDefinitionIdOrderByPriorityAsc(definition.getId());
        response.setConditions(workflowMapper.toConditionResponseList(conditions));

        return response;
    }

    private WorkflowStepResponse enrichStepResponse(WorkflowStep step) {
        WorkflowStepResponse response = workflowMapper.toStepResponse(step);

        if (step.getSpecificApproverEmployeeId() != null) {
            employeeRepository.findById(step.getSpecificApproverEmployeeId())
                    .ifPresent(emp -> response.setSpecificApproverName(emp.getFullName()));
        }

        if (step.getSpecificRoleId() != null) {
            roleRepository.findById(step.getSpecificRoleId())
                    .ifPresent(role -> response.setSpecificRoleName(role.getName()));
        }

        return response;
    }
}
