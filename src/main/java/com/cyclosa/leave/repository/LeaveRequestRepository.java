package com.cyclosa.leave.repository;

import com.cyclosa.leave.entity.LeaveRequest;
import com.cyclosa.leave.enums.LeaveRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, UUID>, JpaSpecificationExecutor<LeaveRequest> {

    Optional<LeaveRequest> findByIdAndCompanyId(UUID id, UUID companyId);

    Optional<LeaveRequest> findByWorkflowInstanceId(UUID workflowInstanceId);

    List<LeaveRequest> findAllByEmployeeId(UUID employeeId);

    List<LeaveRequest> findAllByCompanyId(UUID companyId);

    boolean existsByLeaveTypeId(UUID leaveTypeId);

    /**
     * Kiểm tra trùng lặp khoảng thời gian nghỉ phép của nhân viên với các đơn đang PENDING_APPROVAL hoặc APPROVED.
     */
    @Query("""
        SELECT COUNT(lr) > 0 FROM LeaveRequest lr
        WHERE lr.employeeId = :employeeId
          AND lr.status IN :activeStatuses
          AND lr.startDate <= :endDate
          AND lr.endDate >= :startDate
          AND (:excludeId IS NULL OR lr.id != :excludeId)
    """)
    boolean hasOverlappingRequest(
            @Param("employeeId") UUID employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("activeStatuses") Collection<LeaveRequestStatus> activeStatuses,
            @Param("excludeId") UUID excludeId
    );

    /**
     * Lấy các đơn nghỉ phép APPROVED trong khoảng ngày cho 1 nhân viên.
     */
    @Query("""
        SELECT lr FROM LeaveRequest lr
        WHERE lr.employeeId = :employeeId
          AND lr.status = 'APPROVED'
          AND lr.startDate <= :toDate
          AND lr.endDate >= :fromDate
    """)
    List<LeaveRequest> findApprovedRequestsInRange(
            @Param("employeeId") UUID employeeId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    /**
     * Lấy các đơn nghỉ phép APPROVED trong khoảng ngày cho nhiều nhân viên của công ty.
     */
    @Query("""
        SELECT lr FROM LeaveRequest lr
        WHERE lr.companyId = :companyId
          AND lr.status = 'APPROVED'
          AND lr.startDate <= :toDate
          AND lr.endDate >= :fromDate
    """)
    List<LeaveRequest> findApprovedRequestsByCompanyInRange(
            @Param("companyId") UUID companyId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );
}
