package com.cyclosa.workflow.dto.response;

import com.cyclosa.workflow.enums.RequestType;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowDelegationResponse {
    private UUID id;
    private UUID companyId;
    private UUID delegatorEmployeeId;
    private String delegatorName;
    private UUID delegateeEmployeeId;
    private String delegateeName;
    private RequestType requestType;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
    private Boolean isActive;
    private LocalDateTime createdAt;
}

