package com.cyclosa.contract.dto;

import com.cyclosa.contract.entity.enums.ContractStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ContractAppendixDto {

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AppendixAllowanceDto {
        private UUID id;
        @NotBlank(message = "Tên khoản phụ cấp không được để trống")
        private String name;
        @NotNull(message = "Mức tiền phụ cấp không được để trống")
        private BigDecimal amount;
        private boolean taxable;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateAppendixRequest {
        @NotNull(message = "Ngày có hiệu lực không được để trống")
        private LocalDate effectiveDate;

        @NotBlank(message = "Nội dung điều chỉnh không được để trống")
        private String description;

        private BigDecimal newBaseSalary;
        private BigDecimal newInsuranceSalary;
        private UUID newJobTitleId;
        private UUID newBranchId;
        private String newWorkLocation;
        private UUID templateVersionId;

        @Builder.Default
        private List<AppendixAllowanceDto> allowances = new ArrayList<>();
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateAppendixRequest {
        private LocalDate effectiveDate;
        private String description;
        private BigDecimal newBaseSalary;
        private BigDecimal newInsuranceSalary;
        private UUID newJobTitleId;
        private UUID newBranchId;
        private String newWorkLocation;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContractAppendixResponse {
        private UUID id;
        private UUID contractId;
        private int appendixNo;
        private LocalDate effectiveDate;
        private String description;
        private BigDecimal newBaseSalary;
        private BigDecimal newInsuranceSalary;
        private UUID newJobTitleId;
        private String newJobTitleName;
        private UUID newBranchId;
        private String newBranchName;
        private String newWorkLocation;
        private ContractStatus status;
        private UUID archivedDocumentId;

        @Builder.Default
        private List<AppendixAllowanceDto> allowances = new ArrayList<>();

        private LocalDateTime createdAt;
    }
}

