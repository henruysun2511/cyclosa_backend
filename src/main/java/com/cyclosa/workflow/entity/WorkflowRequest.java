package com.cyclosa.workflow.entity;

import com.cyclosa.common.entity.BaseEntity;
import com.cyclosa.workflow.enums.RequestType;
import com.cyclosa.workflow.enums.StepDecision;
import com.cyclosa.workflow.enums.WorkflowOverallStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "workflow_requests", indexes = {
        @Index(name = "idx_workflow_req_ref", columnList = "reference_id, request_type"),
        @Index(name = "idx_workflow_req_user", columnList = "requested_by_user_id, submitted_at")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE workflow_requests SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class WorkflowRequest extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "definition_id", nullable = false)
    private WorkflowDefinition workflowDefinition;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_type", nullable = false, length = 50)
    private RequestType requestType;

    @Column(name = "reference_id", nullable = false)
    private UUID referenceId;

    @Column(name = "reference_code", length = 100)
    private String referenceCode;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "requested_by_user_id", nullable = false)
    private UUID requestedByUserId;

    @Column(name = "requester_employee_id")
    private UUID requesterEmployeeId;

    @Column(name = "request_amount", precision = 18, scale = 2)
    private BigDecimal requestAmount;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    @Column(name = "submitted_at", nullable = false)
    @Builder.Default
    private LocalDateTime submittedAt = LocalDateTime.now();

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancel_reason", columnDefinition = "TEXT")
    private String cancelReason;

    @OneToMany(mappedBy = "workflowRequest", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("stepOrder ASC")
    @Builder.Default
    private List<WorkflowRequestStep> steps = new ArrayList<>();

    public WorkflowOverallStatus getOverallStatus() {
        if (cancelledAt != null) {
            return WorkflowOverallStatus.CANCELLED;
        }
        if (steps == null || steps.isEmpty()) {
            return WorkflowOverallStatus.APPROVED;
        }
        boolean hasRejected = steps.stream().anyMatch(s -> s.getDecision() == StepDecision.REJECTED);
        if (hasRejected) {
            return WorkflowOverallStatus.REJECTED;
        }
        boolean allFinished = steps.stream().allMatch(s -> s.getDecision() == StepDecision.APPROVED || s.getDecision() == StepDecision.SKIPPED);
        if (allFinished) {
            return WorkflowOverallStatus.APPROVED;
        }
        return WorkflowOverallStatus.PENDING;
    }
}

