package com.cyclosa.workflow.entity;

import com.cyclosa.common.entity.BaseEntity;
import com.cyclosa.workflow.enums.ApproverType;
import com.cyclosa.workflow.enums.StepConditionType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "workflow_definition_steps", indexes = {
        @Index(name = "idx_wf_def_step_order", columnList = "definition_id, step_order")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE workflow_definition_steps SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class WorkflowDefinitionStep extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "definition_id", nullable = false)
    private WorkflowDefinition workflowDefinition;

    @Column(name = "step_order", nullable = false)
    private Integer stepOrder;

    @Column(name = "step_name", nullable = false, length = 150)
    private String stepName;

    @Enumerated(EnumType.STRING)
    @Column(name = "approver_type", nullable = false, length = 50)
    private ApproverType approverType;

    @Column(name = "approver_role_id")
    private UUID approverRoleId;

    @Column(name = "approver_employee_id")
    private UUID approverEmployeeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition_type", nullable = false, length = 50)
    @Builder.Default
    private StepConditionType conditionType = StepConditionType.ALWAYS;

    @Column(name = "condition_threshold", precision = 18, scale = 2)
    private BigDecimal conditionThreshold;
}

