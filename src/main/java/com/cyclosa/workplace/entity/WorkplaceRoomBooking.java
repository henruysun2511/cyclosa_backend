package com.cyclosa.workplace.entity;

import com.cyclosa.common.entity.BaseEntity;
import com.cyclosa.workplace.enums.RoomBookingStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "workplace_room_bookings")
@SQLDelete(sql = "UPDATE workplace_room_bookings SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkplaceRoomBooking extends BaseEntity {

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_object_id", nullable = false)
    private WorkplaceNonDeskObject room;

    @Column(name = "booked_by_employee_id", nullable = false)
    private UUID bookedByEmployeeId;

    @Column(name = "meeting_title", nullable = false, length = 200)
    private String meetingTitle;

    @Column(name = "booking_date", nullable = false)
    private LocalDate bookingDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "participant_count")
    private Integer participantCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private RoomBookingStatus status = RoomBookingStatus.CONFIRMED;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}
