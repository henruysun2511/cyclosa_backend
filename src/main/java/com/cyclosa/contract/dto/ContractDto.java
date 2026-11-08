package com.cyclosa.contract.dto;

import com.cyclosa.contract.entity.enums.ContractStatus;
import com.cyclosa.contract.entity.enums.ContractType;
import com.cyclosa.contract.entity.enums.SalaryPaymentMethod;
import com.cyclosa.contract.entity.enums.SigningForm;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ContractDto {

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AllowanceDto {
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
    public static class CreateContractRequest {
        @NotNull(message = "Nhân viên người lao động không được để trống")
        private UUID employeeId;

        @NotNull(message = "Pháp nhân doanh nghiệp không được để trống")
        private UUID employerCompanyId;

        @NotNull(message = "Người đại diện doanh nghiệp không được để trống")
        private UUID employerRepresentativeId;

        private UUID templateVersionId;

        @NotNull(message = "Loại hợp đồng không được để trống")
        private ContractType contractType;

        private UUID previousContractId;
        private UUID sourceOfferId;

        @NotNull(message = "Chức danh công việc không được để trống")
        private UUID jobTitleId;

        @NotNull(message = "Phòng ban công tác không được để trống")
        private UUID departmentId;

        @NotNull(message = "Chi nhánh làm việc không được để trống")
        private UUID branchId;

        @NotBlank(message = "Địa điểm làm việc không được để trống")
        private String workLocation;

        @NotBlank(message = "Thời giờ làm việc không được để trống")
        private String workingTimeDescription;

        @NotNull(message = "Lương cơ bản không được để trống")
        private BigDecimal baseSalary;

        private BigDecimal insuranceSalary;

        @Builder.Default
        private SalaryPaymentMethod paymentMethod = SalaryPaymentMethod.BANK_TRANSFER;

        @Builder.Default
        private short payDayOfMonth = 5;

        @Builder.Default
        private SigningForm signingForm = SigningForm.ELECTRONIC;

        @NotNull(message = "Ngày bắt đầu có hiệu lực không được để trống")
        private LocalDate startDate;

        private LocalDate endDate;

        @Builder.Default
        private List<AllowanceDto> allowances = new ArrayList<>();
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateContractRequest {
        private String workLocation;
        private String workingTimeDescription;
        private BigDecimal baseSalary;
        private BigDecimal insuranceSalary;
        private SalaryPaymentMethod paymentMethod;
        private Short payDayOfMonth;
        private SigningForm signingForm;
        private LocalDate startDate;
        private LocalDate endDate;
        private List<AllowanceDto> allowances;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContractSummaryResponse {
        private UUID id;
        private String contractNo;
        private UUID employeeId;
        private String employeeCode;
        private String employeeName;
        private UUID employerCompanyId;
        private String companyName;
        private ContractType contractType;
        private String jobTitleName;
        private String departmentName;
        private String branchName;
        private BigDecimal baseSalary;
        private LocalDate startDate;
        private LocalDate endDate;
        private ContractStatus status;
        private SigningForm signingForm;
        private Integer daysUntilExpiry;
        private LocalDateTime createdAt;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContractDetailResponse {
        private UUID id;
        private String contractNo;
        private UUID employeeId;
        private String employeeCode;
        private String employeeName;
        private String employeePhone;
        private String employeeIdentityNumber;

        private UUID employerCompanyId;
        private String companyName;
        private String companyTaxCode;
        private String companyAddress;

        private UUID employerRepresentativeId;
        private String representativeName;

        private UUID templateVersionId;
        private ContractType contractType;
        private UUID previousContractId;

        private UUID jobTitleId;
        private String jobTitleName;
        private UUID departmentId;
        private String departmentName;
        private UUID branchId;
        private String branchName;

        private String workLocation;
        private String workingTimeDescription;
        private BigDecimal baseSalary;
        private BigDecimal insuranceSalary;
        private SalaryPaymentMethod paymentMethod;
        private short payDayOfMonth;
        private SigningForm signingForm;
        private LocalDate startDate;
        private LocalDate endDate;
        private LocalDate signedDate;
        private ContractStatus status;
        private UUID archivedDocumentId;
        private Integer daysUntilExpiry;

        @Builder.Default
        private List<AllowanceDto> allowances = new ArrayList<>();

        @Builder.Default
        private List<ContractAppendixDto.ContractAppendixResponse> appendices = new ArrayList<>();

        @Builder.Default
        private List<ContractSigningDto.ContractSignerResponse> signers = new ArrayList<>();

        private String latestFileUrl;
        private String latestFileHash;
        private LocalDateTime createdAt;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComplianceCheckResponse {
        private boolean compliant;
        @Builder.Default
        private List<String> violations = new ArrayList<>();
        @Builder.Default
        private List<String> warnings = new ArrayList<>();
    }
}

