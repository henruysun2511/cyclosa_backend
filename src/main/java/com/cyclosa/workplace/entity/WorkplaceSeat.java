package com.cyclosa.workplace.entity;

import com.cyclosa.common.entity.BaseEntity;
import com.cyclosa.workplace.enums.DeskShape;
import com.cyclosa.workplace.enums.SeatStatus;
import com.cyclosa.workplace.enums.SeatType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "workplace_seats")
@SQLDelete(sql = "UPDATE workplace_seats SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkplaceSeat extends BaseEntity {

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workplace_row_id", nullable = false)
    private WorkplaceRow row;

    @Column(name = "seat_code", nullable = false, length = 100)
    private String seatCode; // Mã bàn tự sinh, ví dụ: "HN-F8-Z1-R02-S15"

    @Enumerated(EnumType.STRING)
    @Column(name = "seat_type", nullable = false, length = 30)
    @Builder.Default
    private SeatType seatType = SeatType.DEDICATED;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private SeatStatus status = SeatStatus.AVAILABLE;

    @Enumerated(EnumType.STRING)
    @Column(name = "desk_shape", nullable = false, length = 30)
    @Builder.Default
    private DeskShape deskShape = DeskShape.RECTANGLE;

    @Column(name = "position_x", precision = 10, scale = 2, nullable = false)
    private BigDecimal positionX;

    @Column(name = "position_y", precision = 10, scale = 2, nullable = false)
    private BigDecimal positionY;

    @Column(name = "width", precision = 8, scale = 2)
    @Builder.Default
    private BigDecimal width = BigDecimal.valueOf(60);

    @Column(name = "height", precision = 8, scale = 2)
    @Builder.Default
    private BigDecimal height = BigDecimal.valueOf(40);

    @Column(name = "rotation", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal rotation = BigDecimal.ZERO;

    @Column(name = "amenities", columnDefinition = "TEXT")
    private String amenities; // JSON array string: ["2_MONITORS", "STANDING_DESK", "NEAR_WINDOW"]

    @Column(name = "assigned_employee_id")
    private UUID assignedEmployeeId; // Nhân viên ngồi cố định (khi seatType = DEDICATED)

    @Column(name = "qr_code_data", length = 255)
    private String qrCodeData; // Dữ liệu định danh QR code

    @Column(name = "visual_metadata", columnDefinition = "TEXT")
    private String visualMetadata; // JSON mở rộng cho canvas (màu sắc, icon, góc quay ghế...)
}
