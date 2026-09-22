package com.cyclosa.leave.entity;

import com.cyclosa.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "leave_balances", indexes = {
        @Index(name = "idx_leave_balance_emp_year", columnList = "company_id, employee_id, leave_type_id, year", unique = true)
})
@SQLDelete(sql = "UPDATE leave_balances SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveBalance extends BaseEntity {

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "leave_type_id", nullable = false)
    private UUID leaveTypeId;

    @Column(name = "year", nullable = false)
    private Integer year;

    /**
     * Tổng số ngày phép được cấp trong năm:
     * Đã bao gồm thâm niên (Điều 114) và tính theo tỷ lệ tháng nếu chưa đủ 12 tháng.
     */
    @Column(name = "total_days", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal totalDays = BigDecimal.ZERO;

    /**
     * Số ngày phép năm trước chuyển sang (hết hạn ngày 31/03).
     */
    @Column(name = "carried_over_days", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal carriedOverDays = BigDecimal.ZERO;

    /**
     * Số ngày phép đã sử dụng (đã duyệt).
     */
    @Column(name = "used_days", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal usedDays = BigDecimal.ZERO;

    /**
     * Số ngày phép đang chờ duyệt (Pending Approval).
     */
    @Column(name = "pending_days", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal pendingDays = BigDecimal.ZERO;

    /**
     * Số ngày phép khả dụng có thể xin tiếp:
     * Available = (totalDays + carriedOverDays) - (usedDays + pendingDays).
     */
    @Transient
    public BigDecimal getAvailableDays() {
        BigDecimal totalEntitled = (totalDays != null ? totalDays : BigDecimal.ZERO)
                .add(carriedOverDays != null ? carriedOverDays : BigDecimal.ZERO);
        BigDecimal totalCommitted = (usedDays != null ? usedDays : BigDecimal.ZERO)
                .add(pendingDays != null ? pendingDays : BigDecimal.ZERO);
        BigDecimal available = totalEntitled.subtract(totalCommitted);
        return available.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : available;
    }

    /**
     * Số ngày phép còn lại (chưa trừ pending):
     * Remaining = (totalDays + carriedOverDays) - usedDays.
     */
    @Transient
    public BigDecimal getRemainingDays() {
        BigDecimal totalEntitled = (totalDays != null ? totalDays : BigDecimal.ZERO)
                .add(carriedOverDays != null ? carriedOverDays : BigDecimal.ZERO);
        BigDecimal used = usedDays != null ? usedDays : BigDecimal.ZERO;
        BigDecimal remaining = totalEntitled.subtract(used);
        return remaining.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : remaining;
    }
}
