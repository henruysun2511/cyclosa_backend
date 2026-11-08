package com.cyclosa.employee.entity;

import com.cyclosa.common.entity.BaseEntity;
import com.cyclosa.employee.entity.enums.Gender;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "employees", indexes = {
        @Index(name = "idx_employees_company_id", columnList = "company_id"),
        @Index(name = "idx_employees_code", columnList = "employee_code"),
        @Index(name = "idx_employees_user_id", columnList = "user_id"),
        @Index(name = "idx_employees_phone", columnList = "phone"),
        @Index(name = "idx_employees_tax_code", columnList = "personal_tax_code"),
        @Index(name = "idx_employees_insurance", columnList = "social_insurance_number")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE employees SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Employee extends BaseEntity {

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "user_id", unique = true)
    private UUID userId;

    @Column(name = "employee_code", nullable = false, unique = true, length = 30)
    private String employeeCode;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false, length = 20)
    @Builder.Default
    private Gender gender = Gender.OTHER;

    @Column(name = "phone", nullable = false, length = 30)
    private String phone;

    @Column(name = "personal_email")
    private String personalEmail;

    @Column(name = "address", length = 500)
    private String address;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "probation_end_date")
    private LocalDate probationEndDate;

    @Column(name = "personal_tax_code", unique = true, length = 20)
    private String personalTaxCode;

    @Column(name = "social_insurance_number", unique = true, length = 20)
    private String socialInsuranceNumber;

    @Column(name = "portrait_url", length = 500)
    private String portraitUrl;

    @Column(name = "source_application_id", unique = true)
    private UUID sourceApplicationId;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<EmployeeIdentityDocument> identityDocuments = new ArrayList<>();

    @OneToOne(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private EmployeeBankAccount bankAccount;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<EmployeeDependent> dependents = new ArrayList<>();

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<EmployeeEmergencyContact> emergencyContacts = new ArrayList<>();

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("effectiveFrom DESC")
    @Builder.Default
    private List<EmployeeWorkAssignment> workAssignments = new ArrayList<>();

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("effectiveFrom DESC")
    @Builder.Default
    private List<EmployeeStatusHistory> statusHistory = new ArrayList<>();

    public String getFullName() {
        return (lastName + " " + firstName).trim();
    }
}

