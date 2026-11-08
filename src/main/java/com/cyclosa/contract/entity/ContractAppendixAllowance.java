package com.cyclosa.contract.entity;

import com.cyclosa.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;

@Entity
@Table(name = "contract_appendix_allowances", uniqueConstraints = {
        @UniqueConstraint(name = "uq_appendix_allowance", columnNames = {"appendix_id", "name"})
}, indexes = {
        @Index(name = "idx_appendix_allowances_appendix_id", columnList = "appendix_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE contract_appendix_allowances SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class ContractAppendixAllowance extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appendix_id", nullable = false)
    private ContractAppendix appendix;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "amount", nullable = false, precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal amount = BigDecimal.ZERO;

    @Column(name = "is_taxable", nullable = false)
    @Builder.Default
    private boolean taxable = true;
}

