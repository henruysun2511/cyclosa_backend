package com.cyclosa.organization.entity;

import com.cyclosa.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "department_versions", indexes = {
        @Index(name = "idx_dept_vers_dept_id", columnList = "department_id"),
        @Index(name = "idx_dept_vers_effective", columnList = "effective_from"),
        @Index(name = "idx_dept_vers_parent", columnList = "parent_department_id"),
        @Index(name = "idx_dept_vers_deleted", columnList = "deleted")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE department_versions SET deleted = true, deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted = false")
public class DepartmentVersion extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "parent_department_id")
    private UUID parentDepartmentId;

    @Column(name = "cost_center_id")
    private UUID costCenterId;

    @Column(name = "manager_employee_id")
    private UUID managerEmployeeId;

    @Column(name = "is_permission_inherited", nullable = false)
    @Builder.Default
    private Boolean isPermissionInherited = true;

    @Column(name = "deleted", nullable = false)
    @Builder.Default
    private Boolean deleted = false;
}

