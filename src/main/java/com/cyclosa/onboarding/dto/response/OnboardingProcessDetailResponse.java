package com.cyclosa.onboarding.dto.response;

import com.cyclosa.asset.dto.response.AssetAllocationResponse;
import com.cyclosa.common.dto.summary.EmployeeSummary;
import com.cyclosa.common.dto.summary.OrgUnitSummary;
import com.cyclosa.common.dto.summary.PositionSummary;
import com.cyclosa.employee.dto.response.EmployeeDetailResponse;
import com.cyclosa.onboarding.enums.OnboardingStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Chi tiết toàn diện tiến trình Onboarding")
public class OnboardingProcessDetailResponse {

    private UUID id;
    private UUID companyId;
    private UUID employeeId;
    private String employeeName;
    private String employeeCode;
    private EmployeeSummary employee;
    private EmployeeDetailResponse employeeDetail;

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
    private String notes;

    private OnboardingProgressResponse progress;

    @Builder.Default
    private List<OnboardingProcessItemResponse> items = new ArrayList<>();

    @Builder.Default
    private List<AccountProvisioningResponse> accounts = new ArrayList<>();

    @Builder.Default
    private List<OrientationSessionResponse> orientationSessions = new ArrayList<>();

    @Builder.Default
    private List<EmployeeDocumentResponse> documents = new ArrayList<>();

    @Builder.Default
    private List<AssetAllocationResponse> allocatedAssets = new ArrayList<>();

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
