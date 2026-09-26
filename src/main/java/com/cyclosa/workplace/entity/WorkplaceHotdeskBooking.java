package com.cyclosa.workplace.entity;

import com.cyclosa.common.entity.BaseEntity;
import com.cyclosa.workplace.enums.HotdeskBookingStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "workplace_hotdesk_bookings")
@SQLDelete(sql = "UPDATE workplace_hotdesk_bookings SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkplaceHotdeskBooking extends BaseEntity {

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workplace_seat_id", nullable = false)
    private WorkplaceSeat seat;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "booking_date", nullable = false)
    private LocalDate bookingDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private HotdeskBookingStatus status = HotdeskBookingStatus.BOOKED;

    @Column(name = "checked_in_at")
    private java.time.LocalDateTime checkedInAt;

    @Column(name = "notes", length = 255)
    private String notes;
}
