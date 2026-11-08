package com.cyclosa.employee.entity;

import com.cyclosa.common.entity.BaseEntity;
import com.cyclosa.employee.entity.enums.AssignmentChangeReason;
import com.cyclosa.employee.entity.enums.EmploymentType;
import com.cyclosa.organization.entity.Branch;
import com.cyclosa.organization.entity.Department;
import com.cyclosa.organization.entity.JobLevel;
import com.cyclosa.organization.entity.JobTitle;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "employee_work_assignments",
        uniqueConstraints = @UniqueConstraint(name = "uq_employee_work_effective", columnNames = {"employee_id", "effective_from"}),
        indexes = {
                @Index(name = "idx_work_assignments_employee_id", columnList = "employee_id"),
                @Index(name = "idx_work_assignments_effective", columnList = "effective_from"),
                @Index(name = "idx_work_assignments_department", columnList = "department_id"),
                @Index(name = "idx_work_assignments_branch", columnList = "branch_id"),
                @Index(name = "idx_work_assignments_job_title", columnList = "job_title_id"),
                @Index(name = "idx_work_assignments_manager", columnList = "manager_employee_id")
        })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE employee_work_assignments SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class EmployeeWorkAssignment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_title_id", nullable = false)
    private JobTitle jobTitle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_level_id", nullable = false)
    private JobLevel jobLevel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_employee_id")
    private Employee manager;

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_type", nullable = false, length = 30)
    @Builder.Default
    private EmploymentType employmentType = EmploymentType.FULL_TIME;

    @Enumerated(EnumType.STRING)
    @Column(name = "change_reason", nullable = false, length = 30)
    @Builder.Default
    private AssignmentChangeReason changeReason = AssignmentChangeReason.HIRE;

    @Column(name = "transfer_request_id")
    private UUID transferRequestId;
}

