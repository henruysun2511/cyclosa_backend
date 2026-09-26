package com.cyclosa.performance.entity;

import com.cyclosa.common.entity.BaseEntity;
import com.cyclosa.performance.enums.EvaluationStatus;
import com.cyclosa.performance.enums.PerformanceRating;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "performance_evaluations", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"employee_id", "performance_cycle_id"})
})
@SQLDelete(sql = "UPDATE performance_evaluations SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PerformanceEvaluation extends BaseEntity {

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "performance_cycle_id", nullable = false)
    private UUID performanceCycleId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private EvaluationStatus status = EvaluationStatus.DRAFT;

    @Column(name = "final_score", precision = 5, scale = 2)
    private BigDecimal finalScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "rating", length = 30)
    private PerformanceRating rating;

    @Column(name = "self_overall_comment", columnDefinition = "TEXT")
    private String selfOverallComment;

    @Column(name = "manager_overall_comment", columnDefinition = "TEXT")
    private String managerOverallComment;

    @Column(name = "self_submitted_at")
    private LocalDateTime selfSubmittedAt;

    @Column(name = "manager_submitted_at")
    private LocalDateTime managerSubmittedAt;

    @Column(name = "finalized_by_employee_id")
    private UUID finalizedByEmployeeId;

    @Column(name = "finalized_at")
    private LocalDateTime finalizedAt;

    @Column(name = "company_id")
    private UUID companyId;
}
