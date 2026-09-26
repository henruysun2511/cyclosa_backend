package com.cyclosa.onboarding.dto.response;

import com.cyclosa.common.dto.summary.OrgUnitSummary;
import com.cyclosa.common.dto.summary.PositionSummary;
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
@Schema(description = "Thông tin tóm tắt mẫu checklist Onboarding")
public class ChecklistTemplateResponse {

    private UUID id;
    private UUID companyId;
    private String name;
    private UUID applicablePositionId;
    private String applicablePositionTitle;
    private PositionSummary applicablePosition;
    private UUID applicableDepartmentId;
    private String applicableDepartmentName;
    private OrgUnitSummary applicableDepartment;
    private String description;
    private Boolean isActive;
    private Integer totalItems;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
