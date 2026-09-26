package com.cyclosa.workplace.dto.response;

import com.cyclosa.common.dto.summary.BranchSummary;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkplaceSiteResponse {

    private UUID id;
    private UUID companyId;
    private UUID branchId;
    private BranchSummary branch;
    private String name;
    private String code;
    private String address;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;
}
