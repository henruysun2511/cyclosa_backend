package com.cyclosa.offboarding.entity;

import com.cyclosa.common.entity.BaseEntity;
import com.cyclosa.offboarding.enums.TerminationReason;
import com.cyclosa.offboarding.enums.TerminationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "terminations")
@SQLDelete(sql = "UPDATE terminations SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Termination extends BaseEntity {

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "decision_date", nullable = false)
    private LocalDate decisionDate;

    @Column(name = "last_working_date", nullable = false)
    private LocalDate lastWorkingDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "decision_reason_category", nullable = false, length = 40)
    private TerminationReason decisionReasonCategory;

    @Column(name = "decided_by_employee_id")
    private UUID decidedByEmployeeId;

    @Column(name = "discipline_id")
    private UUID disciplineId;

    @Column(name = "contract_termination_id")
    private UUID contractTerminationId;

    @Column(name = "reason_detail", columnDefinition = "TEXT")
    private String reasonDetail;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private TerminationStatus status = TerminationStatus.DRAFT;
}
