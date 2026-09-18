package com.cyclosa.workflow.dto.response;

import com.cyclosa.workflow.enums.ApprovalRequestType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PendingApprovalResponse {
    private UUID workflowInstanceId;
    private UUID approvalStepId;
    private UUID stepId;
    private Integer stepOrder;
    private String stepName;
    private ApprovalRequestType requestType;
    private UUID requestId;
    private UUID requesterEmployeeId;
    private String requesterEmployeeName;
    private UUID assignedApproverEmployeeId;
    private boolean isDelegated;
    private UUID delegatorEmployeeId;
    private String delegatorName;
    private LocalDateTime slaDeadlineAt;
    private LocalDateTime requestCreatedAt;
}
