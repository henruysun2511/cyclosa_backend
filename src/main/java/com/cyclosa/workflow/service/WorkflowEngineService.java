package com.cyclosa.workflow.service;

import com.cyclosa.common.response.PageData;
import com.cyclosa.workflow.dto.request.ApprovalActionRequest;
import com.cyclosa.workflow.dto.request.StartWorkflowRequest;
import com.cyclosa.workflow.dto.response.PendingApprovalResponse;
import com.cyclosa.workflow.dto.response.WorkflowHistoryResponse;
import com.cyclosa.workflow.dto.response.WorkflowInstanceResponse;
import com.cyclosa.workflow.enums.ApprovalRequestType;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface WorkflowEngineService {

    /**
     * Khởi tạo một phiên phê duyệt mới từ quy trình đã published.
     */
    WorkflowInstanceResponse startWorkflow(StartWorkflowRequest request);

    /**
     * Phê duyệt bước hiện tại.
     */
    WorkflowInstanceResponse approve(UUID instanceId, UUID currentEmployeeId, ApprovalActionRequest request);

    /**
     * Từ chối bước hiện tại.
     */
    WorkflowInstanceResponse reject(UUID instanceId, UUID currentEmployeeId, ApprovalActionRequest request);

    /**
     * Hủy yêu cầu phê duyệt (bởi người tạo đơn).
     */
    WorkflowInstanceResponse cancel(UUID instanceId, UUID currentEmployeeId, String reason);

    /**
     * Lấy danh sách yêu cầu đang chờ người dùng duyệt (bao gồm cả các đơn được ủy quyền).
     */
    PageData<PendingApprovalResponse> getPendingApprovals(UUID currentEmployeeId, Pageable pageable);

    /**
     * Lấy lịch sử chi tiết các bước của một workflow instance.
     */
    WorkflowHistoryResponse getInstanceHistory(UUID instanceId);

    /**
     * Tìm workflow instance theo loại yêu cầu và ID bản ghi nghiệp vụ.
     */
    Optional<WorkflowInstanceResponse> getInstanceByRequest(ApprovalRequestType requestType, UUID requestId);
}
