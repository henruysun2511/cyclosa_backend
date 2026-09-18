package com.cyclosa.employee.entity;

import com.cyclosa.common.entity.BaseEntity;
import com.cyclosa.employee.enums.EmployeeChangeType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "employee_history")
@SQLDelete(sql = "UPDATE employee_history SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeHistory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Enumerated(EnumType.STRING)
    @Column(name = "change_type", nullable = false, length = 50)
    private EmployeeChangeType changeType;

    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    @Column(name = "changed_by_employee_id")
    private UUID changedByEmployeeId;

    @Column(columnDefinition = "TEXT")
    private String reason;
}
