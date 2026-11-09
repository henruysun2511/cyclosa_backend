package com.cyclosa.workflow.entity;

import com.cyclosa.common.entity.BaseEntity;
import com.cyclosa.workflow.enums.RequestType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "workflow_delegations", indexes = {
        @Index(name = "idx_wf_delegation_delegator", columnList = "delegator_employee_id, is_active"),
        @Index(name = "idx_wf_delegation_dates", columnList = "start_date, end_date")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE workflow_delegations SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class WorkflowDelegation extends BaseEntity {

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "delegator_employee_id", nullable = false)
    private UUID delegatorEmployeeId;

    @Column(name = "delegatee_employee_id", nullable = false)
    private UUID delegateeEmployeeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_type", length = 50)
    private RequestType requestType;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "reason", nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}

