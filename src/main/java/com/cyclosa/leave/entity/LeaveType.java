package com.cyclosa.leave.entity;

import com.cyclosa.common.entity.BaseEntity;
import com.cyclosa.leave.enums.FundingSource;
import com.cyclosa.leave.enums.LeaveCategory;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "leave_types", indexes = {
        @Index(name = "idx_leave_type_company_code", columnList = "company_id, code", unique = true)
})
@SQLDelete(sql = "UPDATE leave_types SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveType extends BaseEntity {

    @Column(name = "company_id")
    private UUID companyId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 30)
    private LeaveCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "funding_source", nullable = false, length = 30)
    @Builder.Default
    private FundingSource fundingSource = FundingSource.COMPANY;

    /**
     * Dùng khi category = PERSONAL_PAID hoặc PERSONAL_UNPAID.
     * Số ngày cố định theo luật (Điều 115: kết hôn 3 ngày, con kết hôn 1 ngày, tang chế 3 ngày...).
     * Hệ thống tự điền, nhân viên không được tự chỉnh sửa số ngày.
     */
    @Column(name = "fixed_days_per_event", precision = 4, scale = 1)
    private BigDecimal fixedDaysPerEvent;

    @Column(name = "is_paid", nullable = false)
    @Builder.Default
    private Boolean isPaid = true;

    @Column(name = "requires_approval", nullable = false)
    @Builder.Default
    private Boolean requiresApproval = true;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
}
