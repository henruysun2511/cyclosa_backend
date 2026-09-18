package com.cyclosa.workflow.entity;

import com.cyclosa.common.entity.BaseEntity;
import com.cyclosa.workflow.enums.ApprovalRequestType;
import com.cyclosa.workflow.enums.ApprovalStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "workflow_instances", indexes = {
        @Index(name = "idx_workflow_instance_req", columnList = "request_type, request_id"),
        @Index(name = "idx_workflow_instance_company_status", columnList = "company_id, status")
})
@SQLDelete(sql = "UPDATE workflow_instances SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowInstance extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workflow_definition_id", nullable = false)
    private WorkflowDefinition workflowDefinition;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_type", nullable = false, length = 50)
    private ApprovalRequestType requestType;

    @Column(name = "request_id", nullable = false)
    private UUID requestId;

    @Column(name = "requester_employee_id", nullable = false)
    private UUID requesterEmployeeId;

    @Column(name = "current_step_id")
    private UUID currentStepId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private ApprovalStatus status = ApprovalStatus.PENDING;

    @OneToMany(mappedBy = "workflowInstance", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("stepOrder ASC, createdAt ASC")
    @Builder.Default
    private List<WorkflowApprovalStep> approvalSteps = new ArrayList<>();
}
