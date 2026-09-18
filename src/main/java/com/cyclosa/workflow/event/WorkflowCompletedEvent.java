package com.cyclosa.workflow.event;

import com.cyclosa.workflow.enums.ApprovalRequestType;
import com.cyclosa.workflow.enums.ApprovalStatus;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

/**
 * Event phát ra khi một luồng phê duyệt hoàn tất (APPROVED hoặc REJECTED hoặc CANCELLED).
 * Các module nghiệp vụ (Leave, Overtime, Contract, v.v.) lắng nghe event này để tự cập nhật trạng thái bản ghi gốc.
 */
@Getter
@Builder
public class WorkflowCompletedEvent {
    private final UUID workflowInstanceId;
    private final UUID companyId;
    private final ApprovalRequestType requestType;
    private final UUID requestId;
    private final UUID requesterEmployeeId;
    private final ApprovalStatus finalStatus;
    private final String reason;
}
