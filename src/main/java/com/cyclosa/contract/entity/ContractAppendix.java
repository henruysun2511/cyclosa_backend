package com.cyclosa.contract.entity;

import com.cyclosa.common.entity.BaseEntity;
import com.cyclosa.contract.entity.enums.ContractStatus;
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
@Table(name = "contract_appendices", uniqueConstraints = {
        @UniqueConstraint(name = "uq_contract_appendix_no", columnNames = {"contract_id", "appendix_no"})
}, indexes = {
        @Index(name = "idx_contract_appendices_contract", columnList = "contract_id"),
        @Index(name = "idx_contract_appendices_date", columnList = "effective_date")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE contract_appendices SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class ContractAppendix extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;

    @Column(name = "appendix_no", nullable = false)
    private int appendixNo;

    @Column(name = "template_version_id")
    private UUID templateVersionId;

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "new_base_salary", precision = 18, scale = 2)
    private BigDecimal newBaseSalary;

    @Column(name = "new_insurance_salary", precision = 18, scale = 2)
    private BigDecimal newInsuranceSalary;

    @Column(name = "new_job_title_id")
    private UUID newJobTitleId;

    @Column(name = "new_branch_id")
    private UUID newBranchId;

    @Column(name = "new_work_location", length = 500)
    private String newWorkLocation;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private ContractStatus status = ContractStatus.DRAFT;

    @Column(name = "archived_document_id", unique = true)
    private UUID archivedDocumentId;

    @OneToMany(mappedBy = "appendix", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ContractAppendixAllowance> allowances = new ArrayList<>();
}

