package com.cyclosa.onboarding.dto.response;

import com.cyclosa.onboarding.enums.OnboardingStatus;
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
@Schema(description = "Tiến độ chi tiết quy trình Onboarding")
public class OnboardingProgressResponse {

    private UUID onboardingProcessId;
    private UUID employeeId;
    private String employeeName;
    private OnboardingStatus status;
    private long totalItems;
    private long completedItems;
    private long skippedItems;
    private long pendingItems;
    private long incompleteRequiredItems;
    private double progressPercentage;
    private boolean canComplete;
}
