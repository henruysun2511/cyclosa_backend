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
public class WorkflowInstanceResponse {
    private UUID id;
    private UUID workflowDefinitionId;
    private String workflowDefinitionName;
    private Integer workflowVersion;
    private UUID companyId;
    private ApprovalRequestType requestType;
    private UUID requestId;
    private UUID requesterEmployeeId;
    private String requesterEmployeeName;
    private UUID currentStepId;
    private String currentStepName;
    private ApprovalStatus status;
    @Builder.Default
    private List<WorkflowApprovalStepResponse> approvalSteps = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
