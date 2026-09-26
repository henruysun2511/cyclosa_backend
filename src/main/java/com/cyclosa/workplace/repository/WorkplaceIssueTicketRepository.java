package com.cyclosa.workplace.repository;

import com.cyclosa.workplace.entity.WorkplaceIssueTicket;
import com.cyclosa.workplace.enums.IssueSeverity;
import com.cyclosa.workplace.enums.IssueStatus;
import com.cyclosa.workplace.enums.WorkplaceIssueType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkplaceIssueTicketRepository extends JpaRepository<WorkplaceIssueTicket, UUID> {

    @Query("SELECT t FROM WorkplaceIssueTicket t " +
           "LEFT JOIN FETCH t.seat " +
           "LEFT JOIN FETCH t.nonDeskObject " +
           "JOIN FETCH t.floor f " +
           "JOIN FETCH f.site " +
           "WHERE t.id = :id AND t.companyId = :companyId")
    Optional<WorkplaceIssueTicket> findByIdAndCompanyIdWithHierarchy(@Param("id") UUID id, @Param("companyId") UUID companyId);

    Optional<WorkplaceIssueTicket> findByIdAndCompanyId(UUID id, UUID companyId);

    @Query("SELECT t FROM WorkplaceIssueTicket t " +
           "LEFT JOIN FETCH t.seat " +
           "LEFT JOIN FETCH t.nonDeskObject " +
           "JOIN FETCH t.floor f " +
           "JOIN FETCH f.site " +
           "WHERE t.companyId = :companyId " +
           "AND (:floorId IS NULL OR f.id = :floorId) " +
           "AND (:issueType IS NULL OR t.issueType = :issueType) " +
           "AND (:severity IS NULL OR t.severity = :severity) " +
           "AND (:status IS NULL OR t.status = :status) " +
           "AND (:reportedByEmployeeId IS NULL OR t.reportedByEmployeeId = :reportedByEmployeeId)")
    Page<WorkplaceIssueTicket> filterTickets(@Param("companyId") UUID companyId,
                                            @Param("floorId") UUID floorId,
                                            @Param("issueType") WorkplaceIssueType issueType,
                                            @Param("severity") IssueSeverity severity,
                                            @Param("status") IssueStatus status,
                                            @Param("reportedByEmployeeId") UUID reportedByEmployeeId,
                                            Pageable pageable);

    @Query("SELECT COUNT(t) > 0 FROM WorkplaceIssueTicket t " +
           "WHERE t.companyId = :companyId " +
           "AND t.seat.id = :seatId " +
           "AND t.status IN :activeStatuses " +
           "AND (:excludeTicketId IS NULL OR t.id != :excludeTicketId)")
    boolean hasActiveTicketsForSeat(@Param("companyId") UUID companyId,
                                    @Param("seatId") UUID seatId,
                                    @Param("activeStatuses") List<IssueStatus> activeStatuses,
                                    @Param("excludeTicketId") UUID excludeTicketId);

    List<WorkplaceIssueTicket> findByCompanyIdAndReportedByEmployeeIdOrderByCreatedAtDesc(UUID companyId, UUID reportedByEmployeeId);

    @Query("SELECT t FROM WorkplaceIssueTicket t " +
           "LEFT JOIN FETCH t.seat " +
           "LEFT JOIN FETCH t.nonDeskObject " +
           "WHERE t.companyId = :companyId AND t.reportedByEmployeeId = :employeeId ORDER BY t.createdAt DESC")
    List<WorkplaceIssueTicket> findMyTickets(@Param("companyId") UUID companyId, @Param("employeeId") UUID employeeId);
}
