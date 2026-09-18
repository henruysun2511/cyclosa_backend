package com.cyclosa.workflow.service.impl;

import com.cyclosa.common.exception.AppException;
import com.cyclosa.common.response.PageData;
import com.cyclosa.employee.repository.EmployeeRepository;
import com.cyclosa.workflow.dto.request.CreateWorkflowDelegateRequest;
import com.cyclosa.workflow.dto.response.WorkflowDelegateResponse;
import com.cyclosa.workflow.entity.WorkflowDelegate;
import com.cyclosa.workflow.exception.WorkflowErrorCode;
import com.cyclosa.workflow.mapper.WorkflowMapper;
import com.cyclosa.workflow.repository.WorkflowDelegateRepository;
import com.cyclosa.workflow.service.WorkflowDelegateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowDelegateServiceImpl implements WorkflowDelegateService {

    private final WorkflowDelegateRepository delegateRepository;
    private final EmployeeRepository employeeRepository;
    private final WorkflowMapper workflowMapper;

    @Override
    @Transactional
    public WorkflowDelegateResponse createDelegate(CreateWorkflowDelegateRequest request, UUID delegatorEmployeeId) {
        if (request.getDelegateEmployeeId().equals(delegatorEmployeeId)) {
            throw new AppException(WorkflowErrorCode.CANNOT_DELEGATE_TO_SELF);
        }

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new AppException(WorkflowErrorCode.INVALID_DELEGATION_DATE);
        }

        boolean existsOverlapping = delegateRepository.existsOverlapping(
                delegatorEmployeeId, request.getStartDate(), request.getEndDate(), null);
        if (existsOverlapping) {
            throw new AppException(WorkflowErrorCode.OVERLAPPING_DELEGATION);
        }

        WorkflowDelegate delegate = workflowMapper.toDelegateEntity(request);
        delegate.setDelegatorEmployeeId(delegatorEmployeeId);

        WorkflowDelegate saved = delegateRepository.save(delegate);
        log.info("Nhân viên {} đã ủy quyền duyệt phép cho {} từ {} đến {}",
                delegatorEmployeeId, request.getDelegateEmployeeId(), request.getStartDate(), request.getEndDate());

        return enrichDelegateResponse(saved);
    }

    @Override
    @Transactional
    public void cancelDelegate(UUID delegateId, UUID delegatorEmployeeId) {
        WorkflowDelegate delegate = delegateRepository.findById(delegateId)
                .orElseThrow(() -> new AppException(WorkflowErrorCode.WORKFLOW_INSTANCE_NOT_FOUND, "Không tìm thấy ủy quyền"));

        if (!delegate.getDelegatorEmployeeId().equals(delegatorEmployeeId)) {
            throw new AppException(WorkflowErrorCode.NOT_AUTHORIZED_APPROVER, "Bạn không phải người tạo ủy quyền này");
        }

        delegate.setIsActive(false);
        delegateRepository.save(delegate);
        log.info("Đã hủy ủy quyền ID: {}", delegateId);
    }

    @Override
    @Transactional(readOnly = true)
    public PageData<WorkflowDelegateResponse> getMyDelegations(UUID delegatorEmployeeId, Pageable pageable) {
        Page<WorkflowDelegate> page = delegateRepository.findByDelegatorEmployeeId(delegatorEmployeeId, pageable);
        List<WorkflowDelegateResponse> content = page.getContent().stream()
                .map(this::enrichDelegateResponse)
                .toList();
        return PageData.of(page, content);
    }

    @Override
    @Transactional(readOnly = true)
    public PageData<WorkflowDelegateResponse> getDelegationsAssignedToMe(UUID delegateEmployeeId, Pageable pageable) {
        Page<WorkflowDelegate> page = delegateRepository.findByDelegateEmployeeId(delegateEmployeeId, pageable);
        List<WorkflowDelegateResponse> content = page.getContent().stream()
                .map(this::enrichDelegateResponse)
                .toList();
        return PageData.of(page, content);
    }

    private WorkflowDelegateResponse enrichDelegateResponse(WorkflowDelegate delegate) {
        WorkflowDelegateResponse response = workflowMapper.toDelegateResponse(delegate);

        employeeRepository.findById(delegate.getDelegatorEmployeeId())
                .ifPresent(emp -> response.setDelegatorName(emp.getFullName()));

        employeeRepository.findById(delegate.getDelegateEmployeeId())
                .ifPresent(emp -> response.setDelegateName(emp.getFullName()));

        return response;
    }
}
