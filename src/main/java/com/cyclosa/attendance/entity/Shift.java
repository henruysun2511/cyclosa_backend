package com.cyclosa.attendance.entity;

import com.cyclosa.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "shifts", indexes = {
        @Index(name = "idx_shifts_company_code", columnList = "company_id, code")
})
@SQLDelete(sql = "UPDATE shifts SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shift extends BaseEntity {

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "break_start_time")
    private LocalTime breakStartTime;

    @Column(name = "break_end_time")
    private LocalTime breakEndTime;

    @Column(name = "working_hours", nullable = false, precision = 4, scale = 2)
    @Builder.Default
    private BigDecimal workingHours = BigDecimal.valueOf(8.00);

    @Column(name = "work_units", nullable = false, precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal workUnits = BigDecimal.valueOf(1.00);

    @Column(name = "grace_late_minutes", nullable = false)
    @Builder.Default
    private Integer graceLateMinutes = 0;

    @Column(name = "grace_early_minutes", nullable = false)
    @Builder.Default
    private Integer graceEarlyMinutes = 0;

    @Column(name = "is_night_shift", nullable = false)
    @Builder.Default
    private Boolean isNightShift = false;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}
