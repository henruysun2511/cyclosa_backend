package com.cyclosa.recruitment.dto.response;

import com.cyclosa.common.dto.summary.OrgUnitSummary;
import com.cyclosa.common.dto.summary.PositionSummary;
import com.cyclosa.recruitment.enums.JobPositionStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobPositionResponse {
    private UUID id;
    private UUID companyId;
    private String title;
    private PositionSummary position;
    private OrgUnitSummary department;
    private UUID manpowerRequestId;
    private BigDecimal minSalary;
    private BigDecimal maxSalary;
    private JobPositionStatus status;
    private long totalApplications;
    private LocalDateTime createdAt;
}
