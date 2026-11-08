package com.cyclosa.employee.entity;

import com.cyclosa.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "face_enrollments", indexes = {
        @Index(name = "idx_face_enrollments_employee_id", columnList = "employee_id"),
        @Index(name = "idx_face_enrollments_template", columnList = "template_ref")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE face_enrollments SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class FaceEnrollment extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false, unique = true)
    private Employee employee;

    @Column(name = "template_ref", nullable = false, unique = true)
    private String templateRef;

    @Column(name = "quality_score", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal qualityScore = new BigDecimal("85.00");

    @Column(name = "liveness_verified", nullable = false)
    @Builder.Default
    private Boolean livenessVerified = false;

    @Column(name = "consent_given_at", nullable = false)
    private LocalDateTime consentGivenAt;

    @Column(name = "enrolled_at", nullable = false)
    private LocalDateTime enrolledAt;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @Column(name = "revoke_reason", length = 50)
    private String revokeReason;
}

