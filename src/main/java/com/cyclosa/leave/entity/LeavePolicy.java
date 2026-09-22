package com.cyclosa.leave.entity;

import com.cyclosa.common.entity.BaseEntity;
import com.cyclosa.employee.enums.EmploymentType;
import com.cyclosa.leave.enums.JobConditionLevel;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "leave_policies", indexes = {
        @Index(name = "idx_leave_policy_company_type", columnList = "company_id, leave_type_id, job_condition_level")
})
@SQLDelete(sql = "UPDATE leave_policies SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeavePolicy extends BaseEntity {

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "leave_type_id", nullable = false)
    private UUID leaveTypeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "applicable_employment_type", length = 30)
    private EmploymentType applicableEmploymentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_condition_level", length = 30)
    @Builder.Default
    private JobConditionLevel jobConditionLevel = JobConditionLevel.NORMAL;

    /**
     * Số ngày phép năm cơ bản tương ứng với điều kiện làm việc:
     * Bình thường: 12 ngày; Nặng nhọc/độc hại: 14 ngày; Đặc biệt nặng nhọc: 16 ngày (Điều 113).
     */
    @Column(name = "accrual_days_per_year", nullable = false, precision = 4, scale = 1)
    @Builder.Default
    private BigDecimal accrualDaysPerYear = BigDecimal.valueOf(12.0);

    /**
     * Thâm niên làm việc: Cứ đủ N năm làm việc thì được tăng thêm số ngày tương ứng (Điều 114).
     * Mặc định cứ mỗi 5 năm thâm niên cộng 1 ngày phép.
     */
    @Column(name = "seniority_bonus_every_years", nullable = false)
    @Builder.Default
    private Integer seniorityBonusEveryYears = 5;

    @Column(name = "seniority_bonus_days", nullable = false, precision = 4, scale = 1)
    @Builder.Default
    private BigDecimal seniorityBonusDays = BigDecimal.valueOf(1.0);

    /**
     * Số ngày phép tối đa được chuyển sang năm tiếp theo.
     */
    @Column(name = "carry_over_max_days", precision = 4, scale = 1)
    private BigDecimal carryOverMaxDays;

    /**
     * Tháng hết hạn phép chuyển sang năm sau (mặc định tháng 3 -> 31/03).
     */
    @Column(name = "carry_over_expiry_month")
    @Builder.Default
    private Integer carryOverExpiryMonth = 3;

    @Column(name = "effective_date")
    private LocalDate effectiveDate;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}
