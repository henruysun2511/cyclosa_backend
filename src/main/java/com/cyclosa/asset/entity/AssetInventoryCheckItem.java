package com.cyclosa.asset.entity;

import com.cyclosa.asset.enums.InventoryResult;
import com.cyclosa.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "asset_inventory_check_items")
@SQLDelete(sql = "UPDATE asset_inventory_check_items SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetInventoryCheckItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_check_id", nullable = false)
    private AssetInventoryCheck inventoryCheck;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @Enumerated(EnumType.STRING)
    @Column(name = "actual_result", nullable = false, length = 30)
    private InventoryResult actualResult;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;
}
