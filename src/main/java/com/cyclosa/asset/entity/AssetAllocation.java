package com.cyclosa.asset.entity;

import com.cyclosa.asset.enums.AssetCondition;
import com.cyclosa.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "asset_allocations")
@SQLDelete(sql = "UPDATE asset_allocations SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetAllocation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "onboarding_process_id")
    private UUID onboardingProcessId;

    @Column(name = "allocated_date", nullable = false)
    private LocalDate allocatedDate;

    @Column(name = "returned_date")
    private LocalDate returnedDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition_on_allocation", nullable = false, length = 30)
    private AssetCondition conditionOnAllocation;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition_on_return", length = 30)
    private AssetCondition conditionOnReturn;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;
}
