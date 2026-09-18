package com.cyclosa.workflow.entity;

import com.cyclosa.common.entity.BaseEntity;
import com.cyclosa.workflow.enums.WorkflowAction;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "workflow_approval_steps", indexes = {
        @Index(name = "idx_approval_step_assigned", columnList = "assigned_approver_employee_id, action"),
        @Index(name = "idx_approval_step_deadline", columnList = "sla_deadline_at, action")
})
@SQLDelete(sql = "UPDATE workflow_approval_steps SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowApprovalStep extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workflow_instance_id", nullable = false)
    private WorkflowInstance workflowInstance;

    @Column(name = "step_id", nullable = false)
    private UUID stepId;

    @Column(name = "step_order", nullable = false)
    private Integer stepOrder;

    @Column(name = "step_name", length = 150)
    private String stepName;

    @Column(name = "assigned_approver_employee_id")
    private UUID assignedApproverEmployeeId;

    @Column(name = "approver_employee_id")
    private UUID approverEmployeeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 30)
    @Builder.Default
    private WorkflowAction action = WorkflowAction.PENDING;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(name = "sla_deadline_at")
    private LocalDateTime slaDeadlineAt;

    @Column(name = "acted_at")
    private LocalDateTime actedAt;
}
