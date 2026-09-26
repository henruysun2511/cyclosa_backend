package com.cyclosa.workplace.entity;

import com.cyclosa.common.entity.BaseEntity;
import com.cyclosa.workplace.enums.ZoneShapeType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.UUID;

@Entity
@Table(name = "workplace_zones")
@SQLDelete(sql = "UPDATE workplace_zones SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkplaceZone extends BaseEntity {

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workplace_floor_id", nullable = false)
    private WorkplaceFloor floor;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "organizational_unit_id")
    private UUID organizationalUnitId; // Dùng để tô màu theo phòng ban

    @Enumerated(EnumType.STRING)
    @Column(name = "shape_type", nullable = false, length = 30)
    @Builder.Default
    private ZoneShapeType shapeType = ZoneShapeType.RECTANGLE;

    @Column(name = "boundary_coordinates", columnDefinition = "TEXT")
    private String boundaryCoordinates; // JSON array: [{"x": 100, "y": 200}, ...]

    @Column(name = "color_code", length = 30)
    private String colorCode; // Ví dụ: "#3B82F6"

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
}
