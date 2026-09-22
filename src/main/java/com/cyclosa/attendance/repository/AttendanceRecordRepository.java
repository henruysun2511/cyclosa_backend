package com.cyclosa.attendance.repository;

import com.cyclosa.attendance.entity.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, UUID>, JpaSpecificationExecutor<AttendanceRecord> {

    Optional<AttendanceRecord> findByCompanyIdAndEmployeeIdAndWorkDate(UUID companyId, UUID employeeId, LocalDate workDate);

    Optional<AttendanceRecord> findByEmployeeIdAndWorkDate(UUID employeeId, LocalDate workDate);

    Optional<AttendanceRecord> findByIdAndCompanyId(UUID id, UUID companyId);

    List<AttendanceRecord> findAllByEmployeeIdAndWorkDateBetweenOrderByWorkDateAsc(
            UUID employeeId, LocalDate fromDate, LocalDate toDate
    );

    List<AttendanceRecord> findAllByCompanyIdAndWorkDateBetween(
            UUID companyId, LocalDate fromDate, LocalDate toDate
    );

    List<AttendanceRecord> findAllByCompanyIdAndEmployeeIdInAndWorkDateBetween(
            UUID companyId, Set<UUID> employeeIds, LocalDate fromDate, LocalDate toDate
    );

    List<AttendanceRecord> findAllByWorkDateAndCheckInTimeIsNotNullAndCheckOutTimeIsNull(LocalDate workDate);

    List<AttendanceRecord> findAllByWorkDate(LocalDate workDate);

    Optional<AttendanceRecord> findByCompanyIdAndEmployeeIdAndWorkDateAndCheckInTimeIsNotNullAndCheckOutTimeIsNull(
            UUID companyId, UUID employeeId, LocalDate workDate
    );
}
