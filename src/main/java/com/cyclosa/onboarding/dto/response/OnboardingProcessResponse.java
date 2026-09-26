package com.cyclosa.onboarding.dto.response;

import com.cyclosa.common.dto.summary.EmployeeSummary;
import com.cyclosa.common.dto.summary.OrgUnitSummary;
import com.cyclosa.common.dto.summary.PositionSummary;
import com.cyclosa.onboarding.enums.OnboardingStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin tóm tắt quy trình Onboarding")
public class OnboardingProcessResponse {

    private UUID id;
    private UUID companyId;
    private UUID employeeId;
    private String employeeName;
    private String employeeCode;
    private EmployeeSummary employee;

    private String departmentName;
    private OrgUnitSummary department;

    private String positionTitle;
    private PositionSummary position;

    private UUID checklistTemplateId;
    private String checklistTemplateName;
    private LocalDate startDate;
    private OnboardingStatus status;
    private String statusDescription;
    private LocalDateTime completedAt;
    private long totalItems;
    private long completedItems;
    private double progressPercentage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
