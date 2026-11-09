package com.cyclosa.workflow.service;

import com.cyclosa.authorization.entity.Role;
import com.cyclosa.authorization.repository.RoleRepository;
import com.cyclosa.common.exception.AppException;
import com.cyclosa.common.response.PageData;
import com.cyclosa.employee.entity.Employee;
import com.cyclosa.employee.repository.EmployeeRepository;
import com.cyclosa.workflow.dto.request.CreateWorkflowDefinitionRequest;
import com.cyclosa.workflow.dto.request.CreateWorkflowStepRequest;
import com.cyclosa.workflow.dto.request.UpdateWorkflowDefinitionRequest;
import com.cyclosa.workflow.dto.request.WorkflowDefinitionFilterRequest;
import com.cyclosa.workflow.dto.response.WorkflowDefinitionDetailResponse;
import com.cyclosa.workflow.dto.response.WorkflowDefinitionResponse;
import com.cyclosa.workflow.dto.response.WorkflowStepDefinitionResponse;
import com.cyclosa.workflow.entity.WorkflowDefinition;
import com.cyclosa.workflow.entity.WorkflowDefinitionStep;
import com.cyclosa.workflow.exception.WorkflowErrorCode;
import com.cyclosa.workflow.repository.WorkflowDefinitionRepository;
import com.cyclosa.workflow.repository.WorkflowDefinitionStepRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowDefinitionService {

    private final WorkflowDefinitionRepository workflowDefinitionRepository;
    private final WorkflowDefinitionStepRepository workflowDefinitionStepRepository;
    private final RoleRepository roleRepository;
    private final EmployeeRepository employeeRepository;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("createdAt", "name", "requestType", "isActive");

    @Transactional(readOnly = true)
    public PageData<WorkflowDefinitionResponse> search(WorkflowDefinitionFilterRequest filter) {
        Pageable pageable = filter.toPageable("createdAt", ALLOWED_SORT_FIELDS);
        Page<WorkflowDefinition> page = workflowDefinitionRepository.search(
                filter.getCompanyId(),
                filter.getRequestType(),
                filter.getIsActive(),
                filter.getKeyword(),
                pageable
        );

        List<WorkflowDefinitionResponse> content = page.getContent().stream()
                .map(this::toResponse)
                .toList();

        return PageData.of(page, content);
    }

    @Transactional
    public WorkflowDefinitionDetailResponse create(CreateWorkflowDefinitionRequest request) {
        if (request.getSteps() == null || request.getSteps().isEmpty()) {
            throw new AppException(WorkflowErrorCode.WORKFLOW_STEPS_EMPTY);
        }

        // Tự động deactive quy trình cũ nếu đang có quy trình active cùng loại
        Optional<WorkflowDefinition> activeOld = workflowDefinitionRepository
                .findByCompanyIdAndRequestTypeAndIsActiveTrue(request.getCompanyId(), request.getRequestType());
        activeOld.ifPresent(old -> old.setIsActive(false));

        WorkflowDefinition definition = WorkflowDefinition.builder()
                .companyId(request.getCompanyId())
                .requestType(request.getRequestType())
                .name(request.getName().trim())
                .description(request.getDescription())
                .isActive(true)
                .steps(new ArrayList<>())
                .build();

        definition = workflowDefinitionRepository.save(definition);

        int order = 1;
        for (CreateWorkflowStepRequest s : request.getSteps()) {
            WorkflowDefinitionStep step = WorkflowDefinitionStep.builder()
                    .workflowDefinition(definition)
                    .stepOrder(s.getStepOrder() != null ? s.getStepOrder() : order)
                    .stepName(s.getStepName().trim())
                    .approverType(s.getApproverType())
                    .approverRoleId(s.getApproverRoleId())
                    .approverEmployeeId(s.getApproverEmployeeId())
                    .conditionType(s.getConditionType())
                    .conditionThreshold(s.getConditionThreshold())
                    .build();
            definition.getSteps().add(step);
            order++;
        }

        workflowDefinitionRepository.save(definition);
        return getDetail(definition.getId());
    }

    @Transactional(readOnly = true)
    public WorkflowDefinitionDetailResponse getDetail(UUID id) {
        WorkflowDefinition def = workflowDefinitionRepository.findById(id)
                .orElseThrow(() -> new AppException(WorkflowErrorCode.WORKFLOW_DEFINITION_NOT_FOUND));

        List<WorkflowDefinitionStep> steps = workflowDefinitionStepRepository.findByWorkflowDefinitionIdOrderByStepOrderAsc(def.getId());

        List<WorkflowStepDefinitionResponse> stepResponses = steps.stream().map(s -> {
            String roleName = null;
            if (s.getApproverRoleId() != null) {
                roleName = roleRepository.findById(s.getApproverRoleId()).map(Role::getName).orElse(null);
            }
            String empName = null;
            if (s.getApproverEmployeeId() != null) {
                empName = employeeRepository.findById(s.getApproverEmployeeId()).map(Employee::getFullName).orElse(null);
            }
            return WorkflowStepDefinitionResponse.builder()
                    .id(s.getId())
                    .stepOrder(s.getStepOrder())
                    .stepName(s.getStepName())
                    .approverType(s.getApproverType())
                    .approverRoleId(s.getApproverRoleId())
                    .approverRoleName(roleName)
                    .approverEmployeeId(s.getApproverEmployeeId())
                    .approverEmployeeName(empName)
                    .conditionType(s.getConditionType())
                    .conditionThreshold(s.getConditionThreshold())
                    .build();
        }).toList();

        return WorkflowDefinitionDetailResponse.builder()
                .id(def.getId())
                .companyId(def.getCompanyId())
                .requestType(def.getRequestType())
                .name(def.getName())
                .description(def.getDescription())
                .isActive(def.getIsActive())
                .steps(stepResponses)
                .createdAt(def.getCreatedAt())
                .updatedAt(def.getUpdatedAt())
                .build();
    }

    @Transactional
    public WorkflowDefinitionDetailResponse update(UUID id, UpdateWorkflowDefinitionRequest request) {
        WorkflowDefinition def = workflowDefinitionRepository.findById(id)
                .orElseThrow(() -> new AppException(WorkflowErrorCode.WORKFLOW_DEFINITION_NOT_FOUND));

        def.setName(request.getName().trim());
        def.setDescription(request.getDescription());

        // Cập nhật lại danh sách các bước
        workflowDefinitionStepRepository.deleteByWorkflowDefinitionId(def.getId());
        def.getSteps().clear();

        int order = 1;
        for (CreateWorkflowStepRequest s : request.getSteps()) {
            WorkflowDefinitionStep step = WorkflowDefinitionStep.builder()
                    .workflowDefinition(def)
                    .stepOrder(s.getStepOrder() != null ? s.getStepOrder() : order)
                    .stepName(s.getStepName().trim())
                    .approverType(s.getApproverType())
                    .approverRoleId(s.getApproverRoleId())
                    .approverEmployeeId(s.getApproverEmployeeId())
                    .conditionType(s.getConditionType())
                    .conditionThreshold(s.getConditionThreshold())
                    .build();
            def.getSteps().add(step);
            order++;
        }

        workflowDefinitionRepository.save(def);
        return getDetail(def.getId());
    }

    @Transactional
    public void activate(UUID id) {
        WorkflowDefinition target = workflowDefinitionRepository.findById(id)
                .orElseThrow(() -> new AppException(WorkflowErrorCode.WORKFLOW_DEFINITION_NOT_FOUND));

        // Deactivate other definitions with same company and requestType
        List<WorkflowDefinition> existing = workflowDefinitionRepository.findByCompanyIdAndRequestType(target.getCompanyId(), target.getRequestType());
        for (WorkflowDefinition d : existing) {
            d.setIsActive(false);
        }
        target.setIsActive(true);
        workflowDefinitionRepository.save(target);
    }

    @Transactional
    public void deactivate(UUID id) {
        WorkflowDefinition target = workflowDefinitionRepository.findById(id)
                .orElseThrow(() -> new AppException(WorkflowErrorCode.WORKFLOW_DEFINITION_NOT_FOUND));
        target.setIsActive(false);
        workflowDefinitionRepository.save(target);
    }

    @Transactional
    public void delete(UUID id) {
        WorkflowDefinition target = workflowDefinitionRepository.findById(id)
                .orElseThrow(() -> new AppException(WorkflowErrorCode.WORKFLOW_DEFINITION_NOT_FOUND));
        workflowDefinitionRepository.delete(target);
    }

    private WorkflowDefinitionResponse toResponse(WorkflowDefinition d) {
        return WorkflowDefinitionResponse.builder()
                .id(d.getId())
                .companyId(d.getCompanyId())
                .requestType(d.getRequestType())
                .name(d.getName())
                .description(d.getDescription())
                .isActive(d.getIsActive())
                .totalSteps(d.getSteps() != null ? d.getSteps().size() : 0)
                .createdAt(d.getCreatedAt())
                .updatedAt(d.getUpdatedAt())
                .build();
    }
}

