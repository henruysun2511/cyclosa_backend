package com.cyclosa.workplace.entity;

import com.cyclosa.common.entity.BaseEntity;
import com.cyclosa.workplace.enums.ClusterType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "workplace_rows")
@SQLDelete(sql = "UPDATE workplace_rows SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkplaceRow extends BaseEntity {

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workplace_zone_id", nullable = false)
    private WorkplaceZone zone;

    @Column(name = "name", nullable = false, length = 100)
    private String name; // Ví dụ: "R01", "Dãy bàn Backend 1"

    @Enumerated(EnumType.STRING)
    @Column(name = "cluster_type", nullable = false, length = 30)
    @Builder.Default
    private ClusterType clusterType = ClusterType.SINGLE_ROW;

    @Column(name = "position_x", precision = 10, scale = 2)
    private BigDecimal positionX;

    @Column(name = "position_y", precision = 10, scale = 2)
    private BigDecimal positionY;

    @Column(name = "rotation", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal rotation = BigDecimal.ZERO;
}
