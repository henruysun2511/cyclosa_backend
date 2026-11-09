package com.cyclosa.workflow.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowStatsResponse {
    private long pendingMyApproval;
    private long approvedByMe;
    private long mySubmittedTotal;
    private long totalActiveDefinitions;
}

