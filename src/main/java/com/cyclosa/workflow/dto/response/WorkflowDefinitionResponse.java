package com.cyclosa.workflow.dto.response;

import com.cyclosa.workflow.enums.RequestType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowDefinitionResponse {
    private UUID id;
    private UUID companyId;
    private RequestType requestType;
    private String name;
    private String description;
    private Boolean isActive;
    private Integer totalSteps;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

