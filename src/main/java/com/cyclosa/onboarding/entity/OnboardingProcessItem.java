package com.cyclosa.onboarding.entity;

import com.cyclosa.common.entity.BaseEntity;
import com.cyclosa.onboarding.enums.OnboardingItemCategory;
import com.cyclosa.onboarding.enums.OnboardingItemStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "onboarding_process_items")
@SQLDelete(sql = "UPDATE onboarding_process_items SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OnboardingProcessItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "onboarding_process_id", nullable = false)
    private OnboardingProcess onboardingProcess;

    @Column(name = "template_item_id")
    private UUID templateItemId;

    @Column(name = "title", nullable = false, length = 250)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 30)
    private OnboardingItemCategory category;

    @Column(name = "order_index", nullable = false)
    @Builder.Default
    private Integer orderIndex = 0;

    @Column(name = "is_required", nullable = false)
    @Builder.Default
    private Boolean isRequired = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private OnboardingItemStatus status = OnboardingItemStatus.PENDING;

    @Column(name = "completed_by_employee_id")
    private UUID completedByEmployeeId;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;
}
