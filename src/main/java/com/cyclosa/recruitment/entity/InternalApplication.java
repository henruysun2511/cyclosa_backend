package com.cyclosa.recruitment.entity;

import com.cyclosa.common.entity.BaseEntity;
import com.cyclosa.recruitment.enums.InternalApplicationStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "recruitment_internal_applications")
@SQLDelete(sql = "UPDATE recruitment_internal_applications SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InternalApplication extends BaseEntity {

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "opportunity_id", nullable = false)
    private UUID opportunityId;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private InternalApplicationStatus status = InternalApplicationStatus.APPLIED;

    @Column(name = "feedback", columnDefinition = "TEXT")
    private String feedback;
}
