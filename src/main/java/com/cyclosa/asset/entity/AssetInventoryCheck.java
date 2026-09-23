package com.cyclosa.asset.entity;

import com.cyclosa.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "asset_inventory_checks")
@SQLDelete(sql = "UPDATE asset_inventory_checks SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetInventoryCheck extends BaseEntity {

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "title", length = 255)
    private String title;

    @Column(name = "check_date", nullable = false)
    private LocalDate checkDate;

    @Column(name = "performed_by_employee_id", nullable = false)
    private UUID performedByEmployeeId;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @OneToMany(mappedBy = "inventoryCheck", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AssetInventoryCheckItem> items = new ArrayList<>();
}
