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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractDetailResponse {

    private UUID id;
    private String contractNumber;
    private UUID companyId;
    private UUID employeeId;
    private String employeeName;
    private String employeeCode;
    private String employeeDepartment;
    private String employeePosition;
    private ContractType contractType;
    private String contractTypeDescription;
    private ContractStatus contractStatus;
    private String contractStatusDescription;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate signDate;
    private UUID signerEmployeeId;
    private BigDecimal basicSalary;
    private String basicSalaryInWords;
    private BigDecimal insuranceSalary;
    private BigDecimal allowanceLunch;
    private BigDecimal allowancePhone;
    private BigDecimal allowanceTransport;
    private BigDecimal allowanceOther;
    private BigDecimal totalGrossSalary;
    private WorkingHoursType workingHoursType;
    private String workingHoursDescription;
    private String workLocationAddress;
    private String signedContractUrl;
    private Integer definiteContractSequence;
    private UUID workflowInstanceId;
    private String note;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder.Default
    private List<ContractAddendumResponse> addenda = new ArrayList<>();

    private ContractTerminationResponse termination;
}
