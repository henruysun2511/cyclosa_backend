package com.cyclosa.onboarding.entity;

import com.cyclosa.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "onboarding_checklist_templates")
@SQLDelete(sql = "UPDATE onboarding_checklist_templates SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OnboardingChecklistTemplate extends BaseEntity {

    @Column(name = "company_id")
    private UUID companyId;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "applicable_position_id")
    private UUID applicablePositionId;

    @Column(name = "applicable_department_id")
    private UUID applicableDepartmentId;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    @Builder.Default
    private List<OnboardingChecklistTemplateItem> items = new ArrayList<>();
}
