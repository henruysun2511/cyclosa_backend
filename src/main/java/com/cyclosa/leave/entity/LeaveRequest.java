package com.cyclosa.leave.entity;

import com.cyclosa.common.entity.BaseEntity;
import com.cyclosa.leave.enums.LeaveRequestStatus;
import com.cyclosa.leave.enums.LeaveSession;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "leave_requests", indexes = {
        @Index(name = "idx_leave_req_emp_dates", columnList = "company_id, employee_id, start_date, end_date"),
        @Index(name = "idx_leave_req_status", columnList = "status")
})
@SQLDelete(sql = "UPDATE leave_requests SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveRequest extends BaseEntity {

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "leave_type_id", nullable = false)
    private UUID leaveTypeId;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "session", nullable = false, length = 30)
    @Builder.Default
    private LeaveSession session = LeaveSession.FULL_DAY;

    /**
     * Snapshot số ngày công thực trừ (đã loại trừ ngày lễ/cuối tuần).
     * Với nhóm việc riêng (Điều 115): snapshot từ leave_types.fixed_days_per_event.
     */
    @Column(name = "total_days", nullable = false, precision = 4, scale = 1)
    private BigDecimal totalDays;

    @Column(name = "reason", nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private LeaveRequestStatus status = LeaveRequestStatus.PENDING_APPROVAL;

    @Column(name = "workflow_instance_id")
    private UUID workflowInstanceId;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancelled_by")
    private UUID cancelledBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;
}
