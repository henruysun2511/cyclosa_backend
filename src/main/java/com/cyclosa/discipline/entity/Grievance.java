package com.cyclosa.discipline.entity;

import com.cyclosa.common.entity.BaseEntity;
import com.cyclosa.discipline.enums.GrievanceCategory;
import com.cyclosa.discipline.enums.GrievanceStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "grievances")
@SQLDelete(sql = "UPDATE grievances SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Grievance extends BaseEntity {

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "submitted_date", nullable = false)
    private LocalDate submittedDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 40)
    private GrievanceCategory category;

    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private GrievanceStatus status = GrievanceStatus.OPEN;

    @Column(name = "resolution_note", columnDefinition = "TEXT")
    private String resolutionNote;

    @Column(name = "resolved_by_employee_id")
    private UUID resolvedByEmployeeId;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;
}
