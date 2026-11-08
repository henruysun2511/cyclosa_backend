package com.cyclosa.employee.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrollFaceRequest {

    @NotBlank(message = "Mã vector nhận diện không được để trống")
    private String templateRef;

    @Builder.Default
    private BigDecimal qualityScore = new BigDecimal("85.00");

    @Builder.Default
    private Boolean livenessVerified = true;

    @NotNull(message = "Bắt buộc phải có sự đồng thuận xử lý dữ liệu sinh trắc")
    private Boolean consentGiven;
}

