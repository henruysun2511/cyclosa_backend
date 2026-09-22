package com.cyclosa.payroll.dto.response;

import com.cyclosa.common.dto.summary.CompanySummary;
import com.cyclosa.payroll.enums.ComponentType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin chi tiết thành phần lương")
public class SalaryComponentDetailResponse {

    private UUID id;
    private UUID companyId;
    private CompanySummary company;
    private String code;
    private String name;
    private ComponentType componentType;
    private Boolean isTaxable;
    private Boolean isInsuranceBase;
    private Boolean isRecurring;
    private BigDecimal defaultAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
