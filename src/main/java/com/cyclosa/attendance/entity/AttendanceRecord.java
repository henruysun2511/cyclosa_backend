package com.cyclosa.attendance.entity;

import com.cyclosa.attendance.enums.AttendanceStatus;
import com.cyclosa.attendance.enums.CheckMethod;
import com.cyclosa.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "attendance_records", indexes = {
        @Index(name = "idx_att_rec_emp_date", columnList = "company_id, employee_id, work_date"),
        @Index(name = "idx_att_rec_company_date", columnList = "company_id, work_date")
})
@SQLDelete(sql = "UPDATE attendance_records SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceRecord extends BaseEntity {

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "branch_id")
    private UUID branchId;

    @Column(name = "shift_id")
    private UUID shiftId;

    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    @Column(name = "check_in_time")
    private LocalDateTime checkInTime;

    @Column(name = "check_in_lat")
    private Double checkInLat;

    @Column(name = "check_in_long")
    private Double checkInLong;

    @Enumerated(EnumType.STRING)
    @Column(name = "check_in_method", length = 30)
    private CheckMethod checkInMethod;

    @Column(name = "check_out_time")
    private LocalDateTime checkOutTime;

    @Column(name = "check_out_lat")
    private Double checkOutLat;

    @Column(name = "check_out_long")
    private Double checkOutLong;

    @Enumerated(EnumType.STRING)
    @Column(name = "check_out_method", length = 30)
    private CheckMethod checkOutMethod;

    @Column(name = "late_minutes", nullable = false)
    @Builder.Default
    private Integer lateMinutes = 0;

    @Column(name = "early_minutes", nullable = false)
    @Builder.Default
    private Integer earlyMinutes = 0;

    @Column(name = "actual_hours", nullable = false, precision = 4, scale = 2)
    @Builder.Default
    private BigDecimal actualHours = BigDecimal.ZERO;

    @Column(name = "actual_work_units", nullable = false, precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal actualWorkUnits = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private AttendanceStatus status = AttendanceStatus.ABSENT;

    @Column(name = "note", length = 500)
    private String note;
}
