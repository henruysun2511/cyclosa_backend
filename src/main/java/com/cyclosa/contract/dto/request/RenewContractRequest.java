package com.cyclosa.contract.dto.request;

import com.cyclosa.contract.enums.ContractType;
import jakarta.validation.constraints.DecimalMin;
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
public class RenewContractRequest {

    @NotNull(message = "Loại hợp đồng mới không được để trống")
    private ContractType newContractType;

    @NotNull(message = "Ngày bắt đầu hiệu lực mới không được để trống")
    private LocalDate startDate;

    private LocalDate endDate; // Null nếu ký HĐ không xác định thời hạn

    private LocalDate signDate;

    private UUID signerEmployeeId;

    @DecimalMin(value = "0.0", message = "Lương cơ bản không được âm")
    private BigDecimal basicSalary;

    private BigDecimal insuranceSalary;

    private BigDecimal allowanceLunch;

    private BigDecimal allowancePhone;

    private BigDecimal allowanceTransport;

    private BigDecimal allowanceOther;

    private String note;
}
