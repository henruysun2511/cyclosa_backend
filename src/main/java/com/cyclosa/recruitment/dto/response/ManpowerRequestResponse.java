package com.cyclosa.recruitment.dto.response;

import com.cyclosa.common.dto.summary.OrgUnitSummary;
import com.cyclosa.common.dto.summary.PositionSummary;
import com.cyclosa.recruitment.enums.ManpowerRequestStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ManpowerRequestResponse {
    private UUID id;
    private UUID companyId;
    private String requestCode;
    private OrgUnitSummary department;
    private PositionSummary position;
    private Integer quantity;
    private String reason;
    private LocalDate expectedStartDate;
    private ManpowerRequestStatus status;
    private UUID workflowInstanceId;
    private LocalDateTime createdAt;
}
