package com.cyclosa.contract.entity;

import com.cyclosa.common.entity.BaseEntity;
import com.cyclosa.contract.enums.AddendumType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "contract_addenda", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"contract_id", "addendum_number"})
})
@SQLDelete(sql = "UPDATE contract_addenda SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContractAddendum extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;

    @Column(name = "addendum_number", nullable = false, length = 50)
    private String addendumNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "addendum_type", nullable = false, length = 40)
    private AddendumType addendumType;

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    @Column(name = "sign_date")
    private LocalDate signDate;

    @Column(name = "signer_employee_id")
    private UUID signerEmployeeId;

    @Column(name = "new_basic_salary", precision = 15, scale = 2)
    private BigDecimal newBasicSalary;

    @Column(name = "new_allowance_lunch", precision = 15, scale = 2)
    private BigDecimal newAllowanceLunch;

    @Column(name = "new_allowance_phone", precision = 15, scale = 2)
    private BigDecimal newAllowancePhone;

    @Column(name = "new_allowance_transport", precision = 15, scale = 2)
    private BigDecimal newAllowanceTransport;

    @Column(name = "new_allowance_other", precision = 15, scale = 2)
    private BigDecimal newAllowanceOther;

    @Column(name = "new_end_date")
    private LocalDate newEndDate;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "signed_addendum_url", length = 500)
    private String signedAddendumUrl;
}
