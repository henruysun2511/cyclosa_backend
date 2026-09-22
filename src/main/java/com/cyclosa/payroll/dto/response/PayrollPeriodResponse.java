package com.cyclosa.payroll.dto.response;

import com.cyclosa.payroll.enums.PayrollPeriodStatus;
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
@Schema(description = "Thông tin kỳ tính lương")
public class PayrollPeriodResponse {

    private UUID id;
    private UUID companyId;
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
    private UUID workflowInstanceId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
