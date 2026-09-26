package com.cyclosa.workplace.repository;

import com.cyclosa.workplace.entity.WorkplaceRoomBooking;
import com.cyclosa.workplace.enums.RoomBookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkplaceRoomBookingRepository extends JpaRepository<WorkplaceRoomBooking, UUID> {

    @Query("SELECT b FROM WorkplaceRoomBooking b " +
           "JOIN FETCH b.room r " +
           "JOIN FETCH r.zone z " +
           "JOIN FETCH z.floor f " +
           "JOIN FETCH f.site " +
           "WHERE b.id = :id AND b.companyId = :companyId")
    Optional<WorkplaceRoomBooking> findByIdAndCompanyIdWithHierarchy(@Param("id") UUID id, @Param("companyId") UUID companyId);

    Optional<WorkplaceRoomBooking> findByIdAndCompanyId(UUID id, UUID companyId);

    @Query("SELECT COUNT(b) > 0 FROM WorkplaceRoomBooking b " +
           "WHERE b.companyId = :companyId " +
           "AND b.room.id = :roomId " +
           "AND b.bookingDate = :date " +
           "AND b.status IN :activeStatuses " +
           "AND (:excludeBookingId IS NULL OR b.id != :excludeBookingId) " +
           "AND ((b.startTime < :endTime AND b.endTime > :startTime))")
    boolean hasOverlappingBooking(@Param("companyId") UUID companyId,
                                  @Param("roomId") UUID roomId,
                                  @Param("date") LocalDate date,
                                  @Param("startTime") LocalTime startTime,
                                  @Param("endTime") LocalTime endTime,
                                  @Param("activeStatuses") List<RoomBookingStatus> activeStatuses,
                                  @Param("excludeBookingId") UUID excludeBookingId);

    @Query("SELECT b FROM WorkplaceRoomBooking b " +
           "JOIN FETCH b.room r " +
           "WHERE b.companyId = :companyId " +
           "AND b.room.id = :roomId " +
           "AND b.bookingDate = :date " +
           "AND b.status != 'CANCELLED' " +
           "ORDER BY b.startTime ASC")
    List<WorkplaceRoomBooking> findBookingsByRoomAndDate(@Param("companyId") UUID companyId,
                                                         @Param("roomId") UUID roomId,
                                                         @Param("date") LocalDate date);

    @Query("SELECT b FROM WorkplaceRoomBooking b " +
           "JOIN FETCH b.room r " +
           "JOIN FETCH r.zone z " +
           "JOIN FETCH z.floor f " +
           "JOIN FETCH f.site " +
           "WHERE b.companyId = :companyId " +
           "AND (:roomId IS NULL OR b.room.id = :roomId) " +
           "AND (:bookingDate IS NULL OR b.bookingDate = :bookingDate) " +
           "AND (:status IS NULL OR b.status = :status) " +
           "AND (:bookedByEmployeeId IS NULL OR b.bookedByEmployeeId = :bookedByEmployeeId)")
    Page<WorkplaceRoomBooking> filterRoomBookings(@Param("companyId") UUID companyId,
                                                 @Param("roomId") UUID roomId,
                                                 @Param("bookingDate") LocalDate bookingDate,
                                                 @Param("status") RoomBookingStatus status,
                                                 @Param("bookedByEmployeeId") UUID bookedByEmployeeId,
                                                 Pageable pageable);

    @Query("SELECT b FROM WorkplaceRoomBooking b " +
           "JOIN FETCH b.room r " +
           "WHERE b.companyId = :companyId AND b.bookedByEmployeeId = :employeeId ORDER BY b.bookingDate DESC, b.startTime DESC")
    List<WorkplaceRoomBooking> findMyRoomBookings(@Param("companyId") UUID companyId, @Param("employeeId") UUID employeeId);
}
