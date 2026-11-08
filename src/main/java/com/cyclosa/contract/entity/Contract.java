package com.cyclosa.contract.entity;

import com.cyclosa.common.entity.BaseEntity;
import com.cyclosa.contract.entity.enums.ContractStatus;
import com.cyclosa.contract.entity.enums.ContractType;
import com.cyclosa.contract.entity.enums.SalaryPaymentMethod;
import com.cyclosa.contract.entity.enums.SigningForm;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "contracts", indexes = {
        @Index(name = "idx_contracts_no", columnList = "contract_no"),
        @Index(name = "idx_contracts_employee_id", columnList = "employee_id"),
        @Index(name = "idx_contracts_company_id", columnList = "employer_company_id"),
        @Index(name = "idx_contracts_type", columnList = "contract_type"),
        @Index(name = "idx_contracts_status", columnList = "status"),
        @Index(name = "idx_contracts_start_date", columnList = "start_date"),
        @Index(name = "idx_contracts_end_date", columnList = "end_date")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE contracts SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Contract extends BaseEntity {

    @Column(name = "contract_no", nullable = false, unique = true, length = 50)
    private String contractNo;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "employer_company_id", nullable = false)
    private UUID employerCompanyId;

    @Column(name = "employer_representative_id", nullable = false)
    private UUID employerRepresentativeId;

    @Column(name = "template_version_id")
    private UUID templateVersionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "contract_type", nullable = false, length = 30)
    @Builder.Default
    private ContractType contractType = ContractType.FIXED_TERM;

    @Column(name = "previous_contract_id")
    private UUID previousContractId;

    @Column(name = "source_offer_id")
    private UUID sourceOfferId;

    @Column(name = "job_title_id", nullable = false)
    private UUID jobTitleId;

    @Column(name = "department_id", nullable = false)
    private UUID departmentId;

    @Column(name = "branch_id", nullable = false)
    private UUID branchId;

    @Column(name = "work_location", nullable = false, length = 500)
    private String workLocation;

    @Column(name = "working_time_description", nullable = false, length = 500)
    private String workingTimeDescription;

    @Column(name = "base_salary", nullable = false, precision = 18, scale = 2)
    private BigDecimal baseSalary;

    @Column(name = "insurance_salary", precision = 18, scale = 2)
    private BigDecimal insuranceSalary;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 30)
    @Builder.Default
    private SalaryPaymentMethod paymentMethod = SalaryPaymentMethod.BANK_TRANSFER;

    @Column(name = "pay_day_of_month", nullable = false)
    @Builder.Default
    private short payDayOfMonth = 5;

    @Enumerated(EnumType.STRING)
    @Column(name = "signing_form", nullable = false, length = 30)
    @Builder.Default
    private SigningForm signingForm = SigningForm.ELECTRONIC;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "signed_date")
    private LocalDate signedDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private ContractStatus status = ContractStatus.DRAFT;

    @Column(name = "archived_document_id", unique = true)
    private UUID archivedDocumentId;

    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ContractAllowance> allowances = new ArrayList<>();

    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ContractAppendix> appendices = new ArrayList<>();

    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ContractVersion> versions = new ArrayList<>();
}

