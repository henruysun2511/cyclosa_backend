package com.cyclosa.workplace.entity;

import com.cyclosa.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.UUID;

@Entity
@Table(name = "workplace_floors")
@SQLDelete(sql = "UPDATE workplace_floors SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkplaceFloor extends BaseEntity {

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workplace_site_id", nullable = false)
    private WorkplaceSite site;

    @Column(name = "floor_number", nullable = false, length = 50)
    private String floorNumber; // Ví dụ: "8", "F08", "Tầng trệt"

    @Column(name = "name", length = 150)
    private String name; // Ví dụ: "Tầng 8 - Khối Công nghệ"

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
}
