package com.cyclosa.onboarding.dto.response;

import com.cyclosa.onboarding.enums.OnboardingItemCategory;
import com.cyclosa.onboarding.enums.OnboardingItemStatus;
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
@Schema(description = "Thông tin hạng mục công việc trong quy trình Onboarding")
public class OnboardingProcessItemResponse {

    private UUID id;
    private UUID onboardingProcessId;
    private UUID templateItemId;
    private String title;
    private String description;
    private OnboardingItemCategory category;
    private String categoryDescription;
    private Integer orderIndex;
    private Boolean isRequired;
    private OnboardingItemStatus status;
    private String statusDescription;
    private UUID completedByEmployeeId;
    private String completedByEmployeeName;
    private LocalDateTime completedAt;
    private String note;
}
