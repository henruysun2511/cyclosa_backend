package com.cyclosa.workflow.service;

import com.cyclosa.common.response.PageData;
import com.cyclosa.workflow.dto.request.CreateWorkflowDelegateRequest;
import com.cyclosa.workflow.dto.response.WorkflowDelegateResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface WorkflowDelegateService {

    WorkflowDelegateResponse createDelegate(CreateWorkflowDelegateRequest request, UUID delegatorEmployeeId);

    void cancelDelegate(UUID delegateId, UUID delegatorEmployeeId);

    PageData<WorkflowDelegateResponse> getMyDelegations(UUID delegatorEmployeeId, Pageable pageable);

    PageData<WorkflowDelegateResponse> getDelegationsAssignedToMe(UUID delegateEmployeeId, Pageable pageable);
}
