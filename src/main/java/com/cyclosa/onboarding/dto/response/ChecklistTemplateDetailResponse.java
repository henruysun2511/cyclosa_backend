package com.cyclosa.onboarding.dto.response;

import com.cyclosa.common.dto.summary.OrgUnitSummary;
import com.cyclosa.common.dto.summary.PositionSummary;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Chi tiết mẫu checklist Onboarding kèm danh sách hạng mục")
public class ChecklistTemplateDetailResponse {

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
    @Builder.Default
    private List<ChecklistTemplateItemResponse> items = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
