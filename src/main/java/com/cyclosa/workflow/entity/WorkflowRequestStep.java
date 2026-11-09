package com.cyclosa.workflow.entity;

import com.cyclosa.common.entity.BaseEntity;
import com.cyclosa.workflow.enums.StepDecision;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "workflow_request_steps", indexes = {
        @Index(name = "idx_wf_req_step_assignee", columnList = "assignee_employee_id, is_current"),
        @Index(name = "idx_wf_req_step_order", columnList = "request_id, step_order")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE workflow_request_steps SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class WorkflowRequestStep extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private WorkflowRequest workflowRequest;

    @Column(name = "definition_step_id", nullable = false)
    private UUID definitionStepId;

    @Column(name = "step_order", nullable = false)
    private Integer stepOrder;

    @Column(name = "step_name", nullable = false, length = 150)
    private String stepName;

    @Column(name = "assignee_employee_id", nullable = false)
    private UUID assigneeEmployeeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "decision", length = 30)
    private StepDecision decision;

    @Column(name = "decided_at")
    private LocalDateTime decidedAt;

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @Column(name = "is_current", nullable = false)
    @Builder.Default
    private Boolean isCurrent = false;
}

