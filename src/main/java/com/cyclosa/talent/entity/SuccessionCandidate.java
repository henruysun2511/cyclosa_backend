package com.cyclosa.talent.entity;

import com.cyclosa.common.entity.BaseEntity;
import com.cyclosa.talent.enums.SuccessionReadiness;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.UUID;

@Entity
@Table(name = "succession_candidates")
@SQLDelete(sql = "UPDATE succession_candidates SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SuccessionCandidate extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "succession_plan_id", nullable = false)
    private SuccessionPlan successionPlan;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "readiness", nullable = false, length = 30)
    private SuccessionReadiness readiness;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;
}
