package com.cyclosa.workflow.entity;

import com.cyclosa.common.entity.BaseEntity;
import com.cyclosa.workflow.enums.ApprovalRequestType;
import com.cyclosa.workflow.enums.WorkflowStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "workflow_definitions", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"company_id", "request_type", "version"})
})
@SQLDelete(sql = "UPDATE workflow_definitions SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowDefinition extends BaseEntity {

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_type", nullable = false, length = 50)
    private ApprovalRequestType requestType;

    @Column(name = "version", nullable = false)
    @Builder.Default
    private Integer version = 1;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private WorkflowStatus status = WorkflowStatus.DRAFT;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "created_by_employee_id")
    private UUID createdByEmployeeId;

    @Column(columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "workflowDefinition", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("stepOrder ASC")
    @Builder.Default
    private List<WorkflowStep> steps = new ArrayList<>();

    @OneToMany(mappedBy = "workflowDefinition", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("priority ASC")
    @Builder.Default
    private List<WorkflowCondition> conditions = new ArrayList<>();
}
