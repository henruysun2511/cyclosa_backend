package com.cyclosa.workplace.entity;

import com.cyclosa.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "workplace_floor_plans")
@SQLDelete(sql = "UPDATE workplace_floor_plans SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkplaceFloorPlan extends BaseEntity {

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workplace_floor_id", nullable = false)
    private WorkplaceFloor floor;

    @Column(name = "background_image_url", nullable = false, length = 500)
    private String backgroundImageUrl; // URL file ảnh CAD/SVG/PNG nền

    @Column(name = "canvas_width", precision = 10, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal canvasWidth = BigDecimal.valueOf(3000);

    @Column(name = "canvas_height", precision = 10, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal canvasHeight = BigDecimal.valueOf(2000);

    @Column(name = "scale_ratio", precision = 6, scale = 4, nullable = false)
    @Builder.Default
    private BigDecimal scaleRatio = BigDecimal.valueOf(0.05); // 1px = 0.05m = 5cm

    @Column(name = "version", nullable = false)
    @Builder.Default
    private Integer version = 1;

    @Column(name = "is_published", nullable = false)
    @Builder.Default
    private Boolean isPublished = true;
}
