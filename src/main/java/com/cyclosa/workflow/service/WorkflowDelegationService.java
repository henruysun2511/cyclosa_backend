package com.cyclosa.workflow.service;

import com.cyclosa.common.exception.AppException;
import com.cyclosa.common.response.PageData;
import com.cyclosa.common.util.SecurityUtils;
import com.cyclosa.employee.entity.Employee;
import com.cyclosa.employee.repository.EmployeeRepository;
import com.cyclosa.workflow.dto.request.CreateDelegationRequest;
import com.cyclosa.workflow.dto.response.WorkflowDelegationResponse;
import com.cyclosa.workflow.entity.WorkflowDelegation;
import com.cyclosa.workflow.exception.WorkflowErrorCode;
import com.cyclosa.workflow.repository.WorkflowDelegationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowDelegationService {

    private final WorkflowDelegationRepository workflowDelegationRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional
    public WorkflowDelegationResponse create(CreateDelegationRequest request) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        Employee currentEmployee = employeeRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new AppException(WorkflowErrorCode.NOT_ASSIGNED_APPROVER));

        if (currentEmployee.getId().equals(request.getDelegateeEmployeeId())) {
            throw new AppException(WorkflowErrorCode.CANNOT_DELEGATE_TO_SELF);
        }

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new AppException(WorkflowErrorCode.DELEGATION_DATE_INVALID);
        }

        WorkflowDelegation delegation = WorkflowDelegation.builder()
                .companyId(request.getCompanyId())
                .delegatorEmployeeId(currentEmployee.getId())
                .delegateeEmployeeId(request.getDelegateeEmployeeId())
                .requestType(request.getRequestType())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .reason(request.getReason().trim())
                .isActive(true)
                .build();

        delegation = workflowDelegationRepository.save(delegation);
        return toResponse(delegation);
    }

    @Transactional(readOnly = true)
    public PageData<WorkflowDelegationResponse> getMyDelegations(int page, int size) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        Employee currentEmployee = employeeRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new AppException(WorkflowErrorCode.NOT_ASSIGNED_APPROVER));

        Pageable pageable = PageRequest.of(page, size);
        Page<WorkflowDelegation> result = workflowDelegationRepository.findByDelegatorEmployeeIdOrderByCreatedAtDesc(currentEmployee.getId(), pageable);

        List<WorkflowDelegationResponse> list = result.getContent().stream().map(this::toResponse).toList();
        return PageData.of(result, list);
    }

    @Transactional
    public void revoke(UUID id) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        Employee currentEmployee = employeeRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new AppException(WorkflowErrorCode.NOT_ASSIGNED_APPROVER));

        WorkflowDelegation delegation = workflowDelegationRepository.findById(id)
                .orElseThrow(() -> new AppException(WorkflowErrorCode.DELEGATION_NOT_FOUND));

        if (!delegation.getDelegatorEmployeeId().equals(currentEmployee.getId())) {
            throw new AppException(WorkflowErrorCode.NOT_ASSIGNED_APPROVER);
        }

        delegation.setIsActive(false);
        workflowDelegationRepository.save(delegation);
    }

    private WorkflowDelegationResponse toResponse(WorkflowDelegation d) {
        String delegatorName = employeeRepository.findById(d.getDelegatorEmployeeId()).map(Employee::getFullName).orElse("---");
        String delegateeName = employeeRepository.findById(d.getDelegateeEmployeeId()).map(Employee::getFullName).orElse("---");

        return WorkflowDelegationResponse.builder()
                .id(d.getId())
                .companyId(d.getCompanyId())
                .delegatorEmployeeId(d.getDelegatorEmployeeId())
                .delegatorName(delegatorName)
                .delegateeEmployeeId(d.getDelegateeEmployeeId())
                .delegateeName(delegateeName)
                .requestType(d.getRequestType())
                .startDate(d.getStartDate())
                .endDate(d.getEndDate())
                .reason(d.getReason())
                .isActive(d.getIsActive())
                .createdAt(d.getCreatedAt())
                .build();
    }
}

