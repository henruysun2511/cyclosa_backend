package com.cyclosa.offboarding.entity;

import com.cyclosa.common.entity.BaseEntity;
import com.cyclosa.offboarding.enums.ResignationReason;
import com.cyclosa.offboarding.enums.ResignationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "resignations")
@SQLDelete(sql = "UPDATE resignations SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Resignation extends BaseEntity {

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "submitted_date", nullable = false)
    private LocalDate submittedDate;

    @Column(name = "expected_last_working_date", nullable = false)
    private LocalDate expectedLastWorkingDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "personal_reason_category", nullable = false, length = 40)
    private ResignationReason personalReasonCategory;

    @Column(name = "reason_detail", columnDefinition = "TEXT")
    private String reasonDetail;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private ResignationStatus status = ResignationStatus.PENDING;

    @Column(name = "approved_by_employee_id")
    private UUID approvedByEmployeeId;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "contract_termination_id")
    private UUID contractTerminationId;
}
