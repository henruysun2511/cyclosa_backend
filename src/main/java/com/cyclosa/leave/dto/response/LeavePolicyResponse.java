package com.cyclosa.leave.dto.response;

import com.cyclosa.employee.enums.EmploymentType;
import com.cyclosa.leave.enums.JobConditionLevel;
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
@Schema(description = "Thông tin chính sách ngày nghỉ phép")
public class LeavePolicyResponse {

    private UUID id;
    private UUID companyId;
    private UUID leaveTypeId;
    private LeaveTypeResponse leaveType;
    private EmploymentType applicableEmploymentType;
    private JobConditionLevel jobConditionLevel;
    private BigDecimal accrualDaysPerYear;
    private Integer seniorityBonusEveryYears;
    private BigDecimal seniorityBonusDays;
    private BigDecimal carryOverMaxDays;
    private Integer carryOverExpiryMonth;
    private LocalDate effectiveDate;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
