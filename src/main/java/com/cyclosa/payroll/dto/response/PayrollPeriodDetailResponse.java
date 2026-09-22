package com.cyclosa.payroll.dto.response;

import com.cyclosa.common.dto.summary.CompanySummary;
import com.cyclosa.payroll.enums.PayrollPeriodStatus;
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
@Schema(description = "Thông tin chi tiết kỳ tính lương kèm số liệu tổng hợp")
public class PayrollPeriodDetailResponse {

    private UUID id;
    private UUID companyId;
    private CompanySummary company;
    private String name;
    private String code;
    private Integer month;
    private Integer year;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate payDate;
    private BigDecimal standardWorkDays;
    private PayrollPeriodStatus status;
    private BigDecimal totalGross;
    private BigDecimal totalNet;
    private int totalRecordsCount;
    private UUID workflowInstanceId;
    private WorkflowHistoryResponse workflowHistory;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
