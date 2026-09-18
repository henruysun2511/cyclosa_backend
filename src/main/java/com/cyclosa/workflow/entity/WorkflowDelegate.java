package com.cyclosa.workflow.entity;

import com.cyclosa.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "workflow_delegates", indexes = {
        @Index(name = "idx_delegate_delegator_active", columnList = "delegator_employee_id, is_active"),
        @Index(name = "idx_delegate_dates", columnList = "start_date, end_date")
})
@SQLDelete(sql = "UPDATE workflow_delegates SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowDelegate extends BaseEntity {

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "delegator_employee_id", nullable = false)
    private UUID delegatorEmployeeId;

    @Column(name = "delegate_employee_id", nullable = false)
    private UUID delegateEmployeeId;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}
