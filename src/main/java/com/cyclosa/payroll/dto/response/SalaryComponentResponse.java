package com.cyclosa.payroll.dto.response;

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
@Schema(description = "Thông tin thành phần lương")
public class SalaryComponentResponse {

    private UUID id;
    private UUID companyId;
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
