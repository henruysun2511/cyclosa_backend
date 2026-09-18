package com.cyclosa.contract.dto.response;

import com.cyclosa.contract.enums.AddendumType;
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
public class ContractAddendumResponse {

    private UUID id;
    private UUID contractId;
    private String addendumNumber;
    private AddendumType addendumType;
    private String addendumTypeDescription;
    private LocalDate effectiveDate;
    private LocalDate signDate;
    private UUID signerEmployeeId;
    private BigDecimal newBasicSalary;
    private BigDecimal newAllowanceLunch;
    private BigDecimal newAllowancePhone;
    private BigDecimal newAllowanceTransport;
    private BigDecimal newAllowanceOther;
    private LocalDate newEndDate;
    private String content;
    private String signedAddendumUrl;
    private LocalDateTime createdAt;
}
