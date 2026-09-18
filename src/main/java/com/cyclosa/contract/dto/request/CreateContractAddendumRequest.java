package com.cyclosa.contract.dto.request;

import com.cyclosa.contract.enums.AddendumType;
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
public class CreateContractAddendumRequest {

    private String addendumNumber; // Tùy chọn, hệ thống tự sinh nếu để trống

    @NotNull(message = "Loại phụ lục không được để trống")
    private AddendumType addendumType;

    @NotNull(message = "Ngày có hiệu lực không được để trống")
    private LocalDate effectiveDate;

    private LocalDate signDate;

    private UUID signerEmployeeId;

    private BigDecimal newBasicSalary;

    private BigDecimal newAllowanceLunch;

    private BigDecimal newAllowancePhone;

    private BigDecimal newAllowanceTransport;

    private BigDecimal newAllowanceOther;

    private LocalDate newEndDate;

    @NotBlank(message = "Nội dung điều chỉnh phụ lục không được để trống")
    private String content;

    private String signedAddendumUrl;
}
