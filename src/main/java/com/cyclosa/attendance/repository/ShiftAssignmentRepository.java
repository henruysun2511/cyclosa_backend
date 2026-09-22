package com.cyclosa.attendance.repository;

import com.cyclosa.attendance.entity.ShiftAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShiftAssignmentRepository extends JpaRepository<ShiftAssignment, UUID>, JpaSpecificationExecutor<ShiftAssignment> {

    boolean existsByShiftId(UUID shiftId);

    long countByShiftId(UUID shiftId);

    Optional<ShiftAssignment> findByIdAndCompanyId(UUID id, UUID companyId);

    @Query("SELECT sa FROM ShiftAssignment sa JOIN FETCH sa.shift WHERE sa.companyId = :companyId AND sa.employeeId = :employeeId AND sa.assignedDate = :date")
    Optional<ShiftAssignment> findByCompanyIdAndEmployeeIdAndAssignedDateWithShift(
            @Param("companyId") UUID companyId,
            @Param("employeeId") UUID employeeId,
            @Param("date") LocalDate date
    );

    @Query("SELECT sa FROM ShiftAssignment sa JOIN FETCH sa.shift WHERE sa.employeeId = :employeeId AND sa.assignedDate BETWEEN :from AND :to ORDER BY sa.assignedDate ASC")
    List<ShiftAssignment> findByEmployeeIdAndDateRangeWithShift(
            @Param("employeeId") UUID employeeId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );

    void deleteByCompanyIdAndEmployeeIdAndAssignedDate(UUID companyId, UUID employeeId, LocalDate assignedDate);

    List<ShiftAssignment> findAllByAssignedDate(LocalDate assignedDate);

    List<ShiftAssignment> findAllByCompanyIdAndAssignedDate(UUID companyId, LocalDate assignedDate);
}
