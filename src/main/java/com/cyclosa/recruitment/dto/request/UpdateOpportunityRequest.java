package com.cyclosa.recruitment.dto.request;

import com.cyclosa.recruitment.enums.OpportunityStatus;
import com.cyclosa.recruitment.enums.OpportunityType;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOpportunityRequest {

    private String title;

    private OpportunityType type;

    private UUID departmentId;

    private UUID managerId;

    private String description;

    private String requiredSkills;

    private Integer commitmentPercentage;

    private LocalDate startDate;

    private LocalDate endDate;

    private UUID jobPositionId;

    private Integer targetHires;

    private OpportunityStatus status;
}
