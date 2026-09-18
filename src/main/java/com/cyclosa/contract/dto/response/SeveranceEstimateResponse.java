package com.cyclosa.contract.dto.response;

import com.cyclosa.contract.enums.TerminationGround;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeveranceEstimateResponse {

    private TerminationGround terminationGround;
    private String terminationGroundDescription;
    private LocalDate hireDate;
    private LocalDate finalWorkingDate;
    private double totalTenureYears;
    private double bhtnYears;
    private double qualifyingYears;
    private BigDecimal averageSalary;
    private BigDecimal severanceAllowance;
    private BigDecimal lossOfWorkAllowance;
    private boolean eligible;
    private String legalBasis;
    private String note;
}
