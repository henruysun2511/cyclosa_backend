package com.cyclosa.workflow.dto.response;

import com.cyclosa.workflow.enums.ApprovalRequestType;
import com.cyclosa.workflow.enums.WorkflowStatus;
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
public class WorkflowDefinitionResponse {
    private UUID id;
    private UUID companyId;
    private ApprovalRequestType requestType;
    private Integer version;
    private String name;
    private WorkflowStatus status;
    private LocalDateTime publishedAt;
    private UUID createdByEmployeeId;
    private String createdByEmployeeName;
    private String description;
    @Builder.Default
    private List<WorkflowStepResponse> steps = new ArrayList<>();
    @Builder.Default
    private List<WorkflowConditionResponse> conditions = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
