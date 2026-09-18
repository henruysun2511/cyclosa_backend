package com.cyclosa.workflow.dto.response;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowDelegateResponse {
    private UUID id;
    private UUID companyId;
    private UUID delegatorEmployeeId;
    private String delegatorName;
    private UUID delegateEmployeeId;
    private String delegateName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
