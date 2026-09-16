package com.cyclosa.organization.entity;

import com.cyclosa.common.entity.BaseEntity;
import com.cyclosa.common.enums.ActiveStatus;
import com.cyclosa.organization.enums.UnitType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "organizational_unit_histories", indexes = {
        @Index(name = "idx_ouh_unit_effective", columnList = "unit_id, effective_from, effective_to"),
        @Index(name = "idx_ouh_company_effective", columnList = "company_id, effective_from, effective_to")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizationalUnitHistory extends BaseEntity {

    @Column(name = "unit_id", nullable = false)
    private UUID unitId;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "parent_unit_id")
    private UUID parentUnitId;

    @Column(name = "cost_center_id")
    private UUID costCenterId;

    @Column(name = "manager_employee_id")
    private UUID managerEmployeeId;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false, length = 150)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "unit_type", nullable = false, length = 30)
    private UnitType unitType;

    @Column(name = "effective_from", nullable = false)
    private LocalDateTime effectiveFrom;

    @Column(name = "effective_to")
    private LocalDateTime effectiveTo;

    @Column(name = "change_reason", columnDefinition = "TEXT")
    private String changeReason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ActiveStatus status = ActiveStatus.ACTIVE;
}
