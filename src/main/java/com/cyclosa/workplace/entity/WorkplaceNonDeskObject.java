package com.cyclosa.workplace.entity;

import com.cyclosa.common.entity.BaseEntity;
import com.cyclosa.workplace.enums.NonDeskObjectType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "workplace_non_desk_objects")
@SQLDelete(sql = "UPDATE workplace_non_desk_objects SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkplaceNonDeskObject extends BaseEntity {

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workplace_zone_id", nullable = false)
    private WorkplaceZone zone;

    @Enumerated(EnumType.STRING)
    @Column(name = "object_type", nullable = false, length = 30)
    private NonDeskObjectType objectType;

    @Column(name = "name", nullable = false, length = 150)
    private String name; // Ví dụ: "Phòng họp Fansipan", "Khu Pantry tầng 8", "Thang máy sảnh A"

    @Column(name = "position_x", precision = 10, scale = 2, nullable = false)
    private BigDecimal positionX;

    @Column(name = "position_y", precision = 10, scale = 2, nullable = false)
    private BigDecimal positionY;

    @Column(name = "width", precision = 8, scale = 2)
    @Builder.Default
    private BigDecimal width = BigDecimal.valueOf(100);

    @Column(name = "height", precision = 8, scale = 2)
    @Builder.Default
    private BigDecimal height = BigDecimal.valueOf(100);

    @Column(name = "rotation", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal rotation = BigDecimal.ZERO;

    @Column(name = "meeting_room_capacity")
    private Integer meetingRoomCapacity; // Sức chứa nếu là phòng họp

    @Column(name = "qr_code_data", length = 255)
    private String qrCodeData; // Mã QR định danh phòng họp/vật thể

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    public Boolean getIsBookable() {
        return this.objectType == NonDeskObjectType.MEETING_ROOM;
    }
}
