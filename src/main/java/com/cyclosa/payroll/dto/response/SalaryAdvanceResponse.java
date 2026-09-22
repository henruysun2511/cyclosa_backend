package com.cyclosa.payroll.dto.response;

import com.cyclosa.common.dto.summary.EmployeeSummary;
import com.cyclosa.payroll.enums.SalaryAdvanceStatus;
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
@Schema(description = "Thông tin đơn tạm ứng lương")
public class SalaryAdvanceResponse {

    private UUID id;
    private UUID companyId;
    private UUID employeeId;
    private EmployeeSummary employee;
    private LocalDate requestDate;
    private BigDecimal amount;
    private String reason;
    private UUID workflowInstanceId;
    private SalaryAdvanceStatus status;
    private LocalDateTime disbursedAt;
    private UUID deductedPayrollPeriodId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
