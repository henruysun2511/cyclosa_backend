package com.cyclosa.payroll.dto.response;

import com.cyclosa.common.dto.summary.CompanySummary;
import com.cyclosa.common.dto.summary.EmployeeSummary;
import com.cyclosa.payroll.enums.SalaryAdvanceStatus;
import com.cyclosa.workflow.dto.response.WorkflowHistoryResponse;
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
@Schema(description = "Chi tiết đơn tạm ứng lương kèm lịch sử phê duyệt")
public class SalaryAdvanceDetailResponse {

    private UUID id;
    private UUID companyId;
    private CompanySummary company;
    private UUID employeeId;
    private EmployeeSummary employee;
    private LocalDate requestDate;
    private BigDecimal amount;
    private String reason;
    private UUID workflowInstanceId;
    private WorkflowHistoryResponse workflowHistory;
    private SalaryAdvanceStatus status;
    private LocalDateTime disbursedAt;
    private UUID deductedPayrollPeriodId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
