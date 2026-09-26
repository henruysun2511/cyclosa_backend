package com.cyclosa.workplace.entity;

import com.cyclosa.common.entity.BaseEntity;
import com.cyclosa.workplace.enums.IssueSeverity;
import com.cyclosa.workplace.enums.IssueStatus;
import com.cyclosa.workplace.enums.WorkplaceIssueType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "workplace_issue_tickets")
@SQLDelete(sql = "UPDATE workplace_issue_tickets SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkplaceIssueTicket extends BaseEntity {

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workplace_seat_id")
    private WorkplaceSeat seat; // Gắn với bàn nếu sự cố xảy ra tại bàn

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workplace_non_desk_object_id")
    private WorkplaceNonDeskObject nonDeskObject; // Gắn với phòng họp/tiện ích nếu xảy ra tại đó

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workplace_floor_id", nullable = false)
    private WorkplaceFloor floor;

    @Column(name = "reported_by_employee_id", nullable = false)
    private UUID reportedByEmployeeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "issue_type", nullable = false, length = 30)
    private WorkplaceIssueType issueType;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 30)
    @Builder.Default
    private IssueSeverity severity = IssueSeverity.MEDIUM;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "position_x", precision = 10, scale = 2)
    private BigDecimal positionX;

    @Column(name = "position_y", precision = 10, scale = 2)
    private BigDecimal positionY;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private IssueStatus status = IssueStatus.REPORTED;

    @Column(name = "assigned_to_user_id")
    private UUID assignedToUserId; // Kỹ thuật viên / Office Admin phụ trách xử lý

    @Column(name = "resolved_at")
    private java.time.LocalDateTime resolvedAt;

    @Column(name = "resolution_notes", columnDefinition = "TEXT")
    private String resolutionNotes;
}
