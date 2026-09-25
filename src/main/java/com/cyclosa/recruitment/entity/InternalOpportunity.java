package com.cyclosa.recruitment.entity;

import com.cyclosa.common.entity.BaseEntity;
import com.cyclosa.recruitment.enums.OpportunityStatus;
import com.cyclosa.recruitment.enums.OpportunityType;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "recruitment_internal_opportunities")
@SQLDelete(sql = "UPDATE recruitment_internal_opportunities SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InternalOpportunity extends BaseEntity {

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private OpportunityType type;

    @Column(name = "department_id")
    private UUID departmentId;

    @Column(name = "manager_id")
    private UUID managerId;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "required_skills", columnDefinition = "TEXT")
    private String requiredSkills;

    @Builder.Default
    @Column(name = "commitment_percentage")
    private Integer commitmentPercentage = 100;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private OpportunityStatus status = OpportunityStatus.OPEN;

    @Column(name = "job_position_id")
    private UUID jobPositionId;

    @Builder.Default
    @Column(name = "target_hires")
    private Integer targetHires = 1;

    @Builder.Default
    @Column(name = "current_hires")
    private Integer currentHires = 0;
}
