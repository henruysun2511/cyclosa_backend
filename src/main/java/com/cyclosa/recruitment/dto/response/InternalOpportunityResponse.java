package com.cyclosa.recruitment.dto.response;

import com.cyclosa.recruitment.enums.OpportunityStatus;
import com.cyclosa.recruitment.enums.OpportunityType;
import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InternalOpportunityResponse {

    private UUID id;

    private UUID companyId;

    private String title;

    private OpportunityType type;

    private UUID departmentId;

    private String departmentName;

    private UUID managerId;

    private String managerName;

    private String description;

    private String requiredSkills;

    private Integer commitmentPercentage;

    private LocalDate startDate;

    private LocalDate endDate;

    private OpportunityStatus status;

    private UUID jobPositionId;

    private Integer targetHires;

    private Integer currentHires;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
