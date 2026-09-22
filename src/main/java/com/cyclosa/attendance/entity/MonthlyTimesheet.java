package com.cyclosa.attendance.entity;

import com.cyclosa.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "monthly_timesheets", indexes = {
        @Index(name = "idx_m_timesheet_emp_month_year", columnList = "company_id, employee_id, month, year")
})
@SQLDelete(sql = "UPDATE monthly_timesheets SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyTimesheet extends BaseEntity {

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "month", nullable = false)
    private Integer month;

    @Column(name = "year", nullable = false)
    private Integer year;

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

    @Column(name = "total_paid_days", nullable = false, precision = 4, scale = 2)
    @Builder.Default
    private BigDecimal totalPaidDays = BigDecimal.ZERO;

    @Column(name = "total_late_minutes", nullable = false)
    @Builder.Default
    private Integer totalLateMinutes = 0;

    @Column(name = "total_early_minutes", nullable = false)
    @Builder.Default
    private Integer totalEarlyMinutes = 0;

    @Column(name = "missing_punch_count", nullable = false)
    @Builder.Default
    private Integer missingPunchCount = 0;

    @Column(name = "is_locked", nullable = false)
    @Builder.Default
    private Boolean isLocked = false;
}
