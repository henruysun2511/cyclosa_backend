package com.cyclosa.workflow.dto.response;

import com.cyclosa.workflow.enums.ApproverType;
import com.cyclosa.workflow.enums.OverdueAction;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowStepResponse {
    private UUID id;
    private UUID workflowDefinitionId;
    private Integer stepOrder;
    private String name;
    private ApproverType approverType;
    private UUID specificApproverEmployeeId;
    private String specificApproverName;
    private UUID specificRoleId;
    private String specificRoleName;
    private Integer slaHours;
    private OverdueAction overdueAction;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
