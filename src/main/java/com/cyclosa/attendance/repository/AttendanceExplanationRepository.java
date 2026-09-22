package com.cyclosa.attendance.repository;

import com.cyclosa.attendance.entity.AttendanceExplanation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AttendanceExplanationRepository extends JpaRepository<AttendanceExplanation, UUID>, JpaSpecificationExecutor<AttendanceExplanation> {

    Optional<AttendanceExplanation> findByIdAndCompanyId(UUID id, UUID companyId);

    Optional<AttendanceExplanation> findByWorkflowInstanceId(UUID workflowInstanceId);

    Optional<AttendanceExplanation> findByAttendanceRecordId(UUID attendanceRecordId);

    boolean existsByEmployeeIdAndWorkDateAndStatus(
            UUID employeeId,
            java.time.LocalDate workDate,
            com.cyclosa.attendance.enums.ExplanationStatus status
    );
}
