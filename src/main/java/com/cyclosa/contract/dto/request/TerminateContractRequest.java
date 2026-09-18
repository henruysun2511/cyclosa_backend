package com.cyclosa.contract.dto.request;

import com.cyclosa.contract.enums.TerminationGround;
import jakarta.validation.constraints.NotNull;
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
public class TerminateContractRequest {

    @NotNull(message = "Căn cứ chấm dứt hợp đồng không được để trống (Điều 34, 35, 36 BLLĐ 2019)")
    private TerminationGround terminationGround;

    private String decisionNumber;

    @NotNull(message = "Ngày gửi thông báo / nộp đơn không được để trống")
    private LocalDate noticeDate;

    @NotNull(message = "Ngày làm việc cuối cùng không được để trống")
    private LocalDate finalWorkingDate;

    private BigDecimal manualSeveranceAllowance; // Tùy chọn override

    private BigDecimal manualLossOfWorkAllowance; // Tùy chọn override

    private BigDecimal remainingLeavePay;

    private BigDecimal compensationAmount;

    private String reason;

    private UUID approvedByEmployeeId;

    private String signedDecisionUrl;
}
