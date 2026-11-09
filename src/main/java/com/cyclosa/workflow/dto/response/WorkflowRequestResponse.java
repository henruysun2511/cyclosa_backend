package com.cyclosa.workflow.dto.response;

import com.cyclosa.workflow.enums.RequestType;
import com.cyclosa.workflow.enums.WorkflowOverallStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowRequestResponse {
    private UUID id;
    private UUID definitionId;
    private RequestType requestType;
    private UUID referenceId;
    private String referenceCode;
    private String title;
    private UUID requestedByUserId;
    private String requesterName;
    private UUID requesterEmployeeId;
    private BigDecimal requestAmount;
    private WorkflowOverallStatus overallStatus;
    private Integer currentStepOrder;
    private String currentStepName;
    private String currentAssigneeName;
    private LocalDateTime submittedAt;
    private LocalDateTime cancelledAt;
    private String cancelReason;
}

