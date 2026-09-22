package com.cyclosa.attendance.entity;

import com.cyclosa.attendance.enums.ShiftAssignmentStatus;
import com.cyclosa.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "shift_assignments", indexes = {
        @Index(name = "idx_shift_assign_emp_date", columnList = "company_id, employee_id, assigned_date")
})
@SQLDelete(sql = "UPDATE shift_assignments SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShiftAssignment extends BaseEntity {

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_id", nullable = false)
    private Shift shift;

    @Column(name = "assigned_date", nullable = false)
    private LocalDate assignedDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private ShiftAssignmentStatus status = ShiftAssignmentStatus.ASSIGNED;

    @Column(name = "note", length = 255)
    private String note;
}
