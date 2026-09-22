package com.cyclosa.payroll.entity;

import com.cyclosa.common.entity.BaseEntity;
import com.cyclosa.payroll.enums.PayrollRecordStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "payroll_records", indexes = {
        @Index(name = "idx_pr_rec_period_emp", columnList = "payroll_period_id, employee_id"),
        @Index(name = "idx_pr_rec_company_emp", columnList = "company_id, employee_id")
})
@SQLDelete(sql = "UPDATE payroll_records SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayrollRecord extends BaseEntity {

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payroll_period_id", nullable = false)
    private PayrollPeriod payrollPeriod;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "basic_salary", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal basicSalary = BigDecimal.ZERO;

    @Column(name = "insurance_salary", precision = 15, scale = 2)
    private BigDecimal insuranceSalary;

    @Column(name = "standard_work_days", nullable = false, precision = 4, scale = 2)
    @Builder.Default
    private BigDecimal standardWorkDays = BigDecimal.valueOf(22.00);

    @Column(name = "actual_work_days", nullable = false, precision = 4, scale = 2)
    @Builder.Default
    private BigDecimal actualWorkDays = BigDecimal.ZERO;

    @Column(name = "paid_leave_days", nullable = false, precision = 4, scale = 2)
    @Builder.Default
    private BigDecimal paidLeaveDays = BigDecimal.ZERO;

    @Column(name = "unpaid_leave_days", nullable = false, precision = 4, scale = 2)
    @Builder.Default
    private BigDecimal unpaidLeaveDays = BigDecimal.ZERO;

    @Column(name = "time_based_salary", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal timeBasedSalary = BigDecimal.ZERO;

    @Column(name = "overtime_pay", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal overtimePay = BigDecimal.ZERO;

    @Column(name = "allowances_total", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal allowancesTotal = BigDecimal.ZERO;

    @Column(name = "bonus_total", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal bonusTotal = BigDecimal.ZERO;

    @Column(name = "gross_salary", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal grossSalary = BigDecimal.ZERO;

    @Column(name = "social_insurance_employee", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal socialInsuranceEmployee = BigDecimal.ZERO;

    @Column(name = "social_insurance_employer", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal socialInsuranceEmployer = BigDecimal.ZERO;

    @Column(name = "taxable_income", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal taxableIncome = BigDecimal.ZERO;

    @Column(name = "dependents_count", nullable = false)
    @Builder.Default
    private Integer dependentsCount = 0;

    @Column(name = "personal_relief_amount", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal personalReliefAmount = BigDecimal.ZERO;

    @Column(name = "dependent_relief_amount", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal dependentReliefAmount = BigDecimal.ZERO;

    @Column(name = "assessed_income", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal assessedIncome = BigDecimal.ZERO;

    @Column(name = "personal_income_tax", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal personalIncomeTax = BigDecimal.ZERO;

    @Column(name = "advance_deduction", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal advanceDeduction = BigDecimal.ZERO;

    @Column(name = "other_deductions", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal otherDeductions = BigDecimal.ZERO;

    @Column(name = "net_salary", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal netSalary = BigDecimal.ZERO;

    @Column(name = "bank_account_number", length = 50)
    private String bankAccountNumber;

    @Column(name = "bank_name", length = 100)
    private String bankName;

    @Column(name = "bank_branch", length = 150)
    private String bankBranch;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private PayrollRecordStatus status = PayrollRecordStatus.DRAFT;

    @OneToMany(mappedBy = "payrollRecord", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PayrollRecordItem> items = new ArrayList<>();
}
