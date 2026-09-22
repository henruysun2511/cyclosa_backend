package com.cyclosa.contract.dto.response;

import com.cyclosa.contract.enums.ContractStatus;
import com.cyclosa.contract.enums.ContractType;
import com.cyclosa.contract.enums.WorkingHoursType;
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
public class ContractResponse {

    private UUID id;
    private String contractNumber;
    private UUID companyId;
    private UUID employeeId;
    private String employeeName;
    private String employeeCode;
    private ContractType contractType;
    private String contractTypeDescription;
    private ContractStatus contractStatus;
    private String contractStatusDescription;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate signDate;
    private BigDecimal basicSalary;
    private BigDecimal insuranceSalary;
    private BigDecimal allowanceLunch;
    private BigDecimal allowancePhone;
    private BigDecimal allowanceTransport;
    private BigDecimal allowanceOther;
    private WorkingHoursType workingHoursType;
    private Integer definiteContractSequence;
    private String signedContractUrl;
    private LocalDateTime createdAt;
}
