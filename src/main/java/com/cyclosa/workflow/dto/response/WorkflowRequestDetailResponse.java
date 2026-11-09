package com.cyclosa.workflow.dto.response;

import com.cyclosa.workflow.enums.RequestType;
import com.cyclosa.workflow.enums.WorkflowOverallStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowRequestDetailResponse {
    private UUID id;
    private UUID definitionId;
    private String definitionName;
    private RequestType requestType;
    private UUID referenceId;
    private String referenceCode;
    private String title;
    private UUID requestedByUserId;
    private String requesterName;
    private UUID requesterEmployeeId;
    private BigDecimal requestAmount;
    private String metadata;
    private WorkflowOverallStatus overallStatus;
    private LocalDateTime submittedAt;
    private LocalDateTime cancelledAt;
    private String cancelReason;
    private List<WorkflowRequestStepResponse> steps;
}

