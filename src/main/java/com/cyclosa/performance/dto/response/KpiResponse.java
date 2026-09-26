package com.cyclosa.performance.dto.response;

import com.cyclosa.common.dto.summary.CompanySummary;
import com.cyclosa.common.dto.summary.OrgUnitSummary;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin chỉ số KPI")
public class KpiResponse {

    private UUID id;

    private UUID organizationalUnitId;

    private String organizationalUnitName;

    private OrgUnitSummary organizationalUnit;

    private UUID companyId;

    private CompanySummary company;

    private String name;

    private String unit;

    private String description;

    private Boolean isActive;

    private LocalDateTime createdAt;
}
