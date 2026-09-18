package com.cyclosa.workflow.dto.response;

import com.cyclosa.workflow.enums.ApprovalRequestType;
import com.cyclosa.workflow.enums.ApprovalStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowHistoryResponse {
    private UUID instanceId;
    private ApprovalRequestType requestType;
    private UUID requestId;
    private UUID requesterEmployeeId;
    private String requesterEmployeeName;
    private ApprovalStatus status;
    private String workflowDefinitionName;
    private Integer workflowVersion;
    @Builder.Default
    private List<WorkflowApprovalStepResponse> steps = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}
