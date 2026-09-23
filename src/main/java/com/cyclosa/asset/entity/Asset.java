package com.cyclosa.asset.entity;

import com.cyclosa.asset.enums.AssetCategory;
import com.cyclosa.asset.enums.AssetStatus;
import com.cyclosa.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "assets", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"asset_code", "company_id"})
})
@SQLDelete(sql = "UPDATE assets SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Asset extends BaseEntity {

    @Column(name = "asset_code", nullable = false, length = 100)
    private String assetCode;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 40)
    private AssetCategory category;

    @Column(name = "purchase_date")
    private LocalDate purchaseDate;

    @Column(name = "purchase_cost", precision = 15, scale = 2)
    private BigDecimal purchaseCost;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private AssetStatus status = AssetStatus.IN_STOCK;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "serial_number", length = 100)
    private String serialNumber;

    @Column(name = "specifications", columnDefinition = "TEXT")
    private String specifications;

    @Column(name = "warranty_expiry_date")
    private LocalDate warrantyExpiryDate;

    @Column(name = "location", length = 255)
    private String location;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @OneToMany(mappedBy = "asset", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("allocatedDate DESC")
    @Builder.Default
    private List<AssetAllocation> allocations = new ArrayList<>();
}
