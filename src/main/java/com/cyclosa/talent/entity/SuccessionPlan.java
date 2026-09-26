package com.cyclosa.talent.entity;

import com.cyclosa.common.entity.BaseEntity;
import com.cyclosa.talent.enums.SuccessionRisk;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "succession_plans")
@SQLDelete(sql = "UPDATE succession_plans SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SuccessionPlan extends BaseEntity {

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "position_id", nullable = false)
    private UUID positionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false, length = 30)
    private SuccessionRisk riskLevel;

    @Column(name = "review_date")
    private LocalDate reviewDate;

    @OneToMany(mappedBy = "successionPlan", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<SuccessionCandidate> candidates = new ArrayList<>();
}
