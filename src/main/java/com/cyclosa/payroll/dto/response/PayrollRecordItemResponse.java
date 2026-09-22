package com.cyclosa.payroll.dto.response;

import com.cyclosa.payroll.enums.ComponentType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Khoản mục phân rã trên phiếu lương")
public class PayrollRecordItemResponse {

    private UUID id;
    private UUID salaryComponentId;
    private String salaryComponentCode;
    private String salaryComponentName;
    private ComponentType componentType;
    private BigDecimal amount;
    private Boolean isTaxable;
    private Boolean isInsuranceBase;
    private String note;
}
