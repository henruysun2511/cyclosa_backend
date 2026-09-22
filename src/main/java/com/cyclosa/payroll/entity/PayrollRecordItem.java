package com.cyclosa.payroll.entity;

import com.cyclosa.common.entity.BaseEntity;
import com.cyclosa.payroll.enums.ComponentType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "payroll_record_items", indexes = {
        @Index(name = "idx_pr_item_rec", columnList = "payroll_record_id")
})
@SQLDelete(sql = "UPDATE payroll_record_items SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayrollRecordItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payroll_record_id", nullable = false)
    private PayrollRecord payrollRecord;

    @Column(name = "salary_component_id")
    private UUID salaryComponentId;

    @Column(name = "salary_component_code", length = 50)
    private String salaryComponentCode;

    @Column(name = "salary_component_name", nullable = false, length = 200)
    private String salaryComponentName;

    @Enumerated(EnumType.STRING)
    @Column(name = "component_type", nullable = false, length = 30)
    private ComponentType componentType;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal amount = BigDecimal.ZERO;

    @Column(name = "is_taxable", nullable = false)
    @Builder.Default
    private Boolean isTaxable = true;

    @Column(name = "is_insurance_base", nullable = false)
    @Builder.Default
    private Boolean isInsuranceBase = false;

    @Column(name = "note", length = 255)
    private String note;
}
