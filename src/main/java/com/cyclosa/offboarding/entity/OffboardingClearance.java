package com.cyclosa.offboarding.entity;

import com.cyclosa.common.entity.BaseEntity;
import com.cyclosa.offboarding.enums.ClearanceStatus;
import com.cyclosa.offboarding.enums.ClearanceType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "offboarding_clearances")
@SQLDelete(sql = "UPDATE offboarding_clearances SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OffboardingClearance extends BaseEntity {

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "organizational_unit_id")
    private UUID organizationalUnitId;

    @Enumerated(EnumType.STRING)
    @Column(name = "clearance_type", nullable = false, length = 30)
    private ClearanceType clearanceType;

    @Column(name = "cleared_by_employee_id")
    private UUID clearedByEmployeeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private ClearanceStatus status = ClearanceStatus.PENDING;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @Column(name = "cleared_at")
    private LocalDateTime clearedAt;

    @Column(name = "unreturned_asset_count")
    @Builder.Default
    private Integer unreturnedAssetCount = 0;
}
