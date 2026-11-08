package com.cyclosa.employee.entity;

import com.cyclosa.common.entity.BaseEntity;
import com.cyclosa.employee.entity.enums.IdentityDocumentType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;

@Entity
@Table(name = "employee_identity_documents",
        uniqueConstraints = @UniqueConstraint(name = "uq_identity_doc", columnNames = {"document_type", "document_number"}),
        indexes = {
                @Index(name = "idx_identity_docs_employee_id", columnList = "employee_id"),
                @Index(name = "idx_identity_docs_number", columnList = "document_number")
        })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE employee_identity_documents SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class EmployeeIdentityDocument extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, length = 20)
    @Builder.Default
    private IdentityDocumentType documentType = IdentityDocumentType.CITIZEN_ID;

    @Column(name = "document_number", nullable = false, length = 30)
    private String documentNumber;

    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @Column(name = "issue_place", nullable = false, length = 200)
    private String issuePlace;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;
}

