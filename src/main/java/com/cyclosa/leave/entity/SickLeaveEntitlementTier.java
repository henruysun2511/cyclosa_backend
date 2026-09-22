package com.cyclosa.leave.entity;

import com.cyclosa.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;

/**
 * Định mức nghỉ ốm đau theo Luật BHXH Việt Nam:
 * Dưới 15 năm đóng BHXH: 30 ngày (bình thường) / 40 ngày (nặng nhọc).
 * Từ 15 năm đến dưới 30 năm: 40 ngày / 50 ngày.
 * Từ 30 năm trở lên: 60 ngày / 70 ngày.
 */
@Entity
@Table(name = "sick_leave_entitlement_tiers")
@SQLDelete(sql = "UPDATE sick_leave_entitlement_tiers SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SickLeaveEntitlementTier extends BaseEntity {

    @Column(name = "min_bhxh_years", nullable = false, precision = 4, scale = 1)
    private BigDecimal minBhxhYears;

    @Column(name = "max_bhxh_years", precision = 4, scale = 1)
    private BigDecimal maxBhxhYears;

    @Column(name = "normal_condition_max_days", nullable = false)
    private Integer normalConditionMaxDays;

    @Column(name = "hazardous_condition_max_days", nullable = false)
    private Integer hazardousConditionMaxDays;
}
