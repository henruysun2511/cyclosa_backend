package com.cyclosa.workflow.dto.response;

import com.cyclosa.workflow.enums.WorkflowAction;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowApprovalStepResponse {
    private UUID id;
    private UUID stepId;
    private Integer stepOrder;
    private String stepName;
    private UUID assignedApproverEmployeeId;
    private String assignedApproverName;
    private UUID approverEmployeeId;
    private String approverName;
    private WorkflowAction action;
    private String comment;
    private LocalDateTime slaDeadlineAt;
    private LocalDateTime actedAt;
    private LocalDateTime createdAt;
}
