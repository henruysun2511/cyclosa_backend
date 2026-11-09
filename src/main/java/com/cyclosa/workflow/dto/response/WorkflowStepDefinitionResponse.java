package com.cyclosa.workflow.dto.response;

import com.cyclosa.workflow.enums.ApproverType;
import com.cyclosa.workflow.enums.StepConditionType;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowStepDefinitionResponse {
    private UUID id;
    private Integer stepOrder;
    private String stepName;
    private ApproverType approverType;
    private UUID approverRoleId;
    private String approverRoleName;
    private UUID approverEmployeeId;
    private String approverEmployeeName;
    private StepConditionType conditionType;
    private BigDecimal conditionThreshold;
}

