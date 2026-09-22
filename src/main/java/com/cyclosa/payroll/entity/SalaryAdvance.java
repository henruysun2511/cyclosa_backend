package com.cyclosa.payroll.entity;

import com.cyclosa.common.entity.BaseEntity;
import com.cyclosa.payroll.enums.SalaryAdvanceStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "salary_advances", indexes = {
        @Index(name = "idx_sal_adv_emp_date", columnList = "company_id, employee_id, request_date")
})
@SQLDelete(sql = "UPDATE salary_advances SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalaryAdvance extends BaseEntity {

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "request_date", nullable = false)
    private LocalDate requestDate;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "reason", nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Column(name = "workflow_instance_id")
    private UUID workflowInstanceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private SalaryAdvanceStatus status = SalaryAdvanceStatus.DRAFT;

    @Column(name = "disbursed_at")
    private LocalDateTime disbursedAt;

    @Column(name = "deducted_payroll_period_id")
    private UUID deductedPayrollPeriodId;
}
