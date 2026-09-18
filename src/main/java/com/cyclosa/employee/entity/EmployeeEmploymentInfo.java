package com.cyclosa.employee.entity;

import com.cyclosa.common.entity.BaseEntity;
import com.cyclosa.employee.enums.EmploymentType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "employee_employment_info")
@SQLDelete(sql = "UPDATE employee_employment_info SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeEmploymentInfo extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false, unique = true)
    private Employee employee;

    @Column(name = "organizational_unit_id", nullable = false)
    private UUID organizationalUnitId;

    @Column(name = "branch_id", nullable = false)
    private UUID branchId;

    @Column(name = "position_id", nullable = false)
    private UUID positionId;

    @Column(name = "job_level_id")
    private UUID jobLevelId;

    @Column(name = "manager_employee_id")
    private UUID managerEmployeeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_type", nullable = false, length = 30)
    @Builder.Default
    private EmploymentType employmentType = EmploymentType.FULL_TIME;

    @Column(name = "company_email", length = 150)
    private String companyEmail;

    @Column(name = "work_location", length = 255)
    private String workLocation;

    @Column(name = "probation_end_date")
    private LocalDate probationEndDate;
}
