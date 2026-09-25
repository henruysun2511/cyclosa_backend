package com.cyclosa.recruitment.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketplaceStatsResponse {

    private long totalOpportunities;

    private long openOpportunities;

    private long totalApplications;

    private long activeAssignments;

    private long completedAssignments;
}
