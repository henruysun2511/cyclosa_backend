package com.cyclosa.performance.entity;

import com.cyclosa.common.entity.BaseEntity;
import com.cyclosa.performance.enums.GoalStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "goals")
@SQLDelete(sql = "UPDATE goals SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Goal extends BaseEntity {

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "performance_cycle_id", nullable = false)
    private UUID performanceCycleId;

    @Column(name = "kpi_id")
    private UUID kpiId;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "target_value", nullable = false, length = 100)
    private String targetValue;

    @Column(name = "weight_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal weightPercentage;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private GoalStatus status = GoalStatus.NOT_STARTED;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
}
