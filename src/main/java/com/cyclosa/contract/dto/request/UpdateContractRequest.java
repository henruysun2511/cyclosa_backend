package com.cyclosa.contract.dto.request;

import com.cyclosa.contract.enums.WorkingHoursType;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateContractRequest {

    private LocalDate startDate;

    private LocalDate endDate;

    private LocalDate signDate;

    private UUID signerEmployeeId;

    @DecimalMin(value = "0.0", message = "Lương cơ bản không được âm")
    private BigDecimal basicSalary;

    private BigDecimal insuranceSalary;

    private BigDecimal allowanceLunch;

    private BigDecimal allowancePhone;

    private BigDecimal allowanceTransport;

    private BigDecimal allowanceOther;

    private WorkingHoursType workingHoursType;

    private String workLocationAddress;

    private String note;
}
