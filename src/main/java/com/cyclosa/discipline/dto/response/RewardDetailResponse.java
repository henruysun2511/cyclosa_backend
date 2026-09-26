package com.cyclosa.discipline.dto.response;

import com.cyclosa.common.dto.summary.CompanySummary;
import com.cyclosa.common.dto.summary.EmployeeSummary;
import com.cyclosa.common.dto.summary.OrgUnitSummary;
import com.cyclosa.discipline.enums.RewardType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Chi tiết quyết định khen thưởng")
public class RewardDetailResponse {

    private UUID id;
    private UUID employeeId;
    private String employeeName;
    private String employeeCode;
    private EmployeeSummary employee;

    private String departmentName;
    private OrgUnitSummary department;

    private UUID companyId;
    private CompanySummary company;

    private RewardType rewardType;
    private String title;
    private BigDecimal amount;
    private String reason;

    private UUID decidedByEmployeeId;
    private String decidedByEmployeeName;
    private EmployeeSummary decidedBy;

    private LocalDate decidedDate;
    private Boolean pushedToPayroll;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
