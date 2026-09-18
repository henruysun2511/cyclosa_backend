package com.cyclosa.workflow.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowConditionResponse {
    private UUID id;
    private UUID fromStepId;
    private UUID toStepId;
    private String conditionExpression;
    private Integer priority;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
