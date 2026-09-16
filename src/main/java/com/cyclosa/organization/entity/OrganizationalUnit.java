package com.cyclosa.organization.entity;

import com.cyclosa.common.entity.BaseEntity;
import com.cyclosa.common.enums.ActiveStatus;
import com.cyclosa.organization.enums.UnitType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "organizational_units", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"code", "company_id"})
})
@SQLDelete(sql = "UPDATE organizational_units SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizationalUnit extends BaseEntity {

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false, length = 150)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "unit_type", nullable = false, length = 30)
    @Builder.Default
    private UnitType unitType = UnitType.DEPARTMENT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_unit_id")
    private OrganizationalUnit parentUnit;

    @OneToMany(mappedBy = "parentUnit", fetch = FetchType.LAZY)
    @Builder.Default
    private List<OrganizationalUnit> children = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cost_center_id")
    private CostCenter costCenter;

    @Column(name = "manager_employee_id")
    private UUID managerEmployeeId;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ActiveStatus status = ActiveStatus.ACTIVE;
}
