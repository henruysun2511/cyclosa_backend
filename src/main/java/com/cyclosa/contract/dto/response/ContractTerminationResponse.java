package com.cyclosa.contract.dto.response;

import com.cyclosa.contract.enums.TerminationGround;
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
public class ContractTerminationResponse {

    private UUID id;
    private UUID contractId;
    private TerminationGround terminationGround;
    private String terminationGroundDescription;
    private String decisionNumber;
    private LocalDate noticeDate;
    private LocalDate finalWorkingDate;
    private BigDecimal severanceAllowance;
    private BigDecimal lossOfWorkAllowance;
    private BigDecimal remainingLeavePay;
    private BigDecimal compensationAmount;
    private BigDecimal totalPayout;
    private boolean unlawfulTermination;
    private String reason;
    private UUID approvedByEmployeeId;
    private String signedDecisionUrl;
    private LocalDateTime createdAt;
}
