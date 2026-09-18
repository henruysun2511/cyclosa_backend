package com.cyclosa.workflow.entity;

import com.cyclosa.common.entity.BaseEntity;
import com.cyclosa.workflow.enums.ApproverType;
import com.cyclosa.workflow.enums.OverdueAction;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.UUID;

@Entity
@Table(name = "workflow_steps")
@SQLDelete(sql = "UPDATE workflow_steps SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowStep extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workflow_definition_id", nullable = false)
    private WorkflowDefinition workflowDefinition;

    @Column(name = "step_order", nullable = false)
    private Integer stepOrder;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "approver_type", nullable = false, length = 50)
    private ApproverType approverType;

    @Column(name = "specific_approver_employee_id")
    private UUID specificApproverEmployeeId;

    @Column(name = "specific_role_id")
    private UUID specificRoleId;

    @Column(name = "sla_hours")
    private Integer slaHours;

    @Enumerated(EnumType.STRING)
    @Column(name = "overdue_action", length = 30)
    @Builder.Default
    private OverdueAction overdueAction = OverdueAction.REMIND;
}
