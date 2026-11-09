package com.cyclosa.workflow.dto.response;

import com.cyclosa.workflow.enums.RequestType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowDefinitionDetailResponse {
    private UUID id;
    private UUID companyId;
    private RequestType requestType;
    private String name;
    private String description;
    private Boolean isActive;
    private List<WorkflowStepDefinitionResponse> steps;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

