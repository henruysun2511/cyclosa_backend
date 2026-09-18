package com.cyclosa.workflow.event;

import com.cyclosa.workflow.enums.ApprovalRequestType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event phát ra khi một bước duyệt được giao cho người phê duyệt mới (để tích hợp gửi thông báo/email ở Module 19).
 */
@Getter
@Builder
public class WorkflowStepAssignedEvent {
    private final UUID workflowInstanceId;
    private final UUID stepId;
    private final String stepName;
    private final Integer stepOrder;
    private final ApprovalRequestType requestType;
    private final UUID requestId;
    private final UUID requesterEmployeeId;
    private final UUID assignedApproverEmployeeId;
    private final LocalDateTime slaDeadlineAt;
}
