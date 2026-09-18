package com.cyclosa.employee.entity;

import com.cyclosa.common.entity.BaseEntity;
import com.cyclosa.employee.enums.FamilyRelationship;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;

@Entity
@Table(name = "employee_dependents")
@SQLDelete(sql = "UPDATE employee_dependents SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeDependent extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private FamilyRelationship relationship;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Column(name = "national_id_number", length = 30)
    private String nationalIdNumber;

    @Column(name = "tax_deduction_registered", nullable = false)
    @Builder.Default
    private boolean taxDeductionRegistered = false;
}
