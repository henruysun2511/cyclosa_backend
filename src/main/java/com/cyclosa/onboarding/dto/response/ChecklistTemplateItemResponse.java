package com.cyclosa.onboarding.dto.response;

import com.cyclosa.onboarding.enums.OnboardingItemCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin hạng mục công việc trong mẫu Onboarding")
public class ChecklistTemplateItemResponse {

    private UUID id;
    private UUID templateId;
    private String title;
    private String description;
    private OnboardingItemCategory category;
    private String categoryDescription;
    private Integer orderIndex;
    private Boolean isRequired;
}
