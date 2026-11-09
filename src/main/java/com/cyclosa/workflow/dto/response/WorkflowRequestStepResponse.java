package com.cyclosa.workflow.dto.response;

import com.cyclosa.workflow.enums.StepDecision;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowRequestStepResponse {
    private UUID id;
    private UUID definitionStepId;
    private Integer stepOrder;
    private String stepName;
    private UUID assigneeEmployeeId;
    private String assigneeName;
    private String assigneeCode;
    private String assigneeJobTitle;
    private StepDecision decision;
    private LocalDateTime decidedAt;
    private String comment;
    private Boolean isCurrent;
}

