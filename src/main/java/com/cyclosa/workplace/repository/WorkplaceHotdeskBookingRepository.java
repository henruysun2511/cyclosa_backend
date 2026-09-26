package com.cyclosa.workplace.repository;

import com.cyclosa.workplace.entity.WorkplaceHotdeskBooking;
import com.cyclosa.workplace.enums.HotdeskBookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkplaceHotdeskBookingRepository extends JpaRepository<WorkplaceHotdeskBooking, UUID> {

    @Query("SELECT b FROM WorkplaceHotdeskBooking b " +
           "JOIN FETCH b.seat s " +
           "JOIN FETCH s.row r " +
           "JOIN FETCH r.zone z " +
           "JOIN FETCH z.floor f " +
           "JOIN FETCH f.site " +
           "WHERE b.id = :id AND b.companyId = :companyId")
    Optional<WorkplaceHotdeskBooking> findByIdAndCompanyIdWithHierarchy(@Param("id") UUID id, @Param("companyId") UUID companyId);

    Optional<WorkplaceHotdeskBooking> findByIdAndCompanyId(UUID id, UUID companyId);

    boolean existsByCompanyIdAndSeatIdAndBookingDateAndStatusIn(UUID companyId, UUID seatId, LocalDate date, List<HotdeskBookingStatus> statuses);

    boolean existsByCompanyIdAndEmployeeIdAndBookingDateAndStatusIn(UUID companyId, UUID employeeId, LocalDate date, List<HotdeskBookingStatus> statuses);

    @Query("SELECT b FROM WorkplaceHotdeskBooking b " +
           "JOIN FETCH b.seat s " +
           "JOIN FETCH s.row r " +
           "JOIN FETCH r.zone z " +
           "JOIN FETCH z.floor f " +
           "JOIN FETCH f.site " +
           "WHERE b.companyId = :companyId " +
           "AND (:employeeId IS NULL OR b.employeeId = :employeeId) " +
           "AND (:seatId IS NULL OR b.seat.id = :seatId) " +
           "AND (:bookingDate IS NULL OR b.bookingDate = :bookingDate) " +
           "AND (:status IS NULL OR b.status = :status) " +
           "AND (:floorId IS NULL OR f.id = :floorId)")
    Page<WorkplaceHotdeskBooking> filterBookings(@Param("companyId") UUID companyId,
                                                @Param("employeeId") UUID employeeId,
                                                @Param("seatId") UUID seatId,
                                                @Param("bookingDate") LocalDate bookingDate,
                                                @Param("status") HotdeskBookingStatus status,
                                                @Param("floorId") UUID floorId,
                                                Pageable pageable);

    @Query("SELECT b FROM WorkplaceHotdeskBooking b " +
           "JOIN FETCH b.seat s " +
           "WHERE b.companyId = :companyId AND b.employeeId = :employeeId ORDER BY b.bookingDate DESC")
    List<WorkplaceHotdeskBooking> findMyBookings(@Param("companyId") UUID companyId, @Param("employeeId") UUID employeeId);

    @Query("SELECT b FROM WorkplaceHotdeskBooking b " +
           "WHERE b.companyId = :companyId AND b.seat.id = :seatId AND b.bookingDate = :date AND b.status IN :statuses")
    Optional<WorkplaceHotdeskBooking> findActiveBookingForSeat(@Param("companyId") UUID companyId,
                                                              @Param("seatId") UUID seatId,
                                                              @Param("date") LocalDate date,
                                                              @Param("statuses") List<HotdeskBookingStatus> statuses);
}
