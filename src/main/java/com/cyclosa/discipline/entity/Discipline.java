package com.cyclosa.discipline.entity;

import com.cyclosa.common.entity.BaseEntity;
import com.cyclosa.discipline.enums.DisciplineStatus;
import com.cyclosa.discipline.enums.DisciplineType;
import com.cyclosa.discipline.enums.DismissalGround;
import com.cyclosa.discipline.enums.ViolationCategory;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "disciplines")
@SQLDelete(sql = "UPDATE disciplines SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Discipline extends BaseEntity {

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "violation_date", nullable = false)
    private LocalDate violationDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "violation_category", nullable = false, length = 40)
    private ViolationCategory violationCategory;

    @Column(name = "statute_of_limitation_deadline", nullable = false)
    private LocalDate statuteOfLimitationDeadline;

    @Column(name = "evidence_files", columnDefinition = "TEXT")
    private String evidenceFiles;

    @Column(name = "handbook_reference", length = 255)
    private String handbookReference;

    @Column(name = "meeting_date")
    private LocalDate meetingDate;

    @Column(name = "meeting_attendees", columnDefinition = "TEXT")
    private String meetingAttendees;

    @Enumerated(EnumType.STRING)
    @Column(name = "discipline_type", nullable = false, length = 40)
    private DisciplineType disciplineType;

    @Enumerated(EnumType.STRING)
    @Column(name = "dismissal_ground", length = 60)
    private DismissalGround dismissalGround;

    @Column(name = "salary_extension_months")
    private Integer salaryExtensionMonths;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "decision_date")
    private LocalDate decisionDate;

    @Column(name = "decided_by_employee_id")
    private UUID decidedByEmployeeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private DisciplineStatus status = DisciplineStatus.DRAFT;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "file_url", length = 500)
    private String fileUrl;
}
