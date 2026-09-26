package com.cyclosa.offboarding.dto.response;

import com.cyclosa.employee.enums.EmploymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OffboardingSummaryResponse {

    private UUID employeeId;

    private String employeeName;

    private String employeeCode;

    private String departmentName;

    private String positionName;

    private EmploymentStatus employmentStatus;

    private LocalDate lastWorkingDate;

    private String offboardingType; // RESIGNATION or TERMINATION

    private String reason;

    private Boolean isFullyCleared;

    private Long unreturnedAssetCount;

    private BigDecimal remainingAnnualLeaveDays;

    private List<OffboardingClearanceResponse> clearances;

    private ExitInterviewResponse exitInterview;
}
