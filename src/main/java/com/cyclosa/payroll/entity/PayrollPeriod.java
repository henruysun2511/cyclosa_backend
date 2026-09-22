package com.cyclosa.payroll.entity;

import com.cyclosa.common.entity.BaseEntity;
import com.cyclosa.payroll.enums.PayrollPeriodStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "payroll_periods", indexes = {
        @Index(name = "idx_pr_period_company_code", columnList = "company_id, code"),
        @Index(name = "idx_pr_period_company_month_year", columnList = "company_id, month, year")
})
@SQLDelete(sql = "UPDATE payroll_periods SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayrollPeriod extends BaseEntity {

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Column(name = "month", nullable = false)
    private Integer month;

    @Column(name = "year", nullable = false)
    private Integer year;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "pay_date")
    private LocalDate payDate;

    @Column(name = "standard_work_days", nullable = false, precision = 4, scale = 2)
    @Builder.Default
    private BigDecimal standardWorkDays = BigDecimal.valueOf(22.00);

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private PayrollPeriodStatus status = PayrollPeriodStatus.OPEN;

    @Column(name = "total_gross", nullable = false, precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal totalGross = BigDecimal.ZERO;

    @Column(name = "total_net", nullable = false, precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal totalNet = BigDecimal.ZERO;

    @Column(name = "workflow_instance_id")
    private UUID workflowInstanceId;

    @Version
    @Column(name = "version")
    private Integer version;
}
