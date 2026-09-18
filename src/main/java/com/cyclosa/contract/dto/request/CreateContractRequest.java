package com.cyclosa.contract.dto.request;

import com.cyclosa.contract.enums.ContractType;
import com.cyclosa.contract.enums.WorkingHoursType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
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
public class CreateContractRequest {

    private String contractNumber; // Nếu để trống, hệ thống sẽ tự sinh

    @NotNull(message = "ID công ty không được để trống")
    private UUID companyId;

    @NotNull(message = "ID nhân viên không được để trống")
    private UUID employeeId;

    @NotNull(message = "Loại hợp đồng không được để trống")
    private ContractType contractType;

    @NotNull(message = "Ngày bắt đầu hiệu lực không được để trống")
    private LocalDate startDate;

    private LocalDate endDate; // Bắt buộc nếu DEFINITE_TERM hoặc PROBATION, để null nếu INDEFINITE_TERM

    private LocalDate signDate;

    private UUID signerEmployeeId;

    @NotNull(message = "Mức lương cơ bản không được để trống")
    @DecimalMin(value = "0.0", message = "Lương cơ bản không được âm")
    private BigDecimal basicSalary;

    private BigDecimal insuranceSalary;

    private BigDecimal allowanceLunch;

    private BigDecimal allowancePhone;

    private BigDecimal allowanceTransport;

    private BigDecimal allowanceOther;

    @Builder.Default
    private WorkingHoursType workingHoursType = WorkingHoursType.STANDARD_44H;

    private String workLocationAddress;

    private String note;

    private UUID templateId; // Tùy chọn: áp dụng mẫu hợp đồng cụ thể
}
