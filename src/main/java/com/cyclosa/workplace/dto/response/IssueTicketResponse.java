package com.cyclosa.workplace.dto.response;

import com.cyclosa.common.dto.summary.EmployeeSummary;
import com.cyclosa.workplace.enums.IssueSeverity;
import com.cyclosa.workplace.enums.IssueStatus;
import com.cyclosa.workplace.enums.WorkplaceIssueType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IssueTicketResponse {

    private UUID id;
    private UUID companyId;
    private UUID seatId;
    private String seatCode;
    private UUID nonDeskObjectId;
    private String nonDeskObjectName;
    private UUID floorId;
    private String floorNumber;
    private UUID reportedByEmployeeId;
    private EmployeeSummary reportedByEmployee;
    private WorkplaceIssueType issueType;
    private IssueSeverity severity;
    private String title;
    private String description;
    private BigDecimal positionX;
    private BigDecimal positionY;
    private IssueStatus status;
    private UUID assignedToUserId;
    private LocalDateTime resolvedAt;
    private String resolutionNotes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
