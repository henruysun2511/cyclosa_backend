package com.cyclosa.employee.entity;

import com.cyclosa.common.entity.BaseEntity;
import com.cyclosa.employee.enums.Gender;
import com.cyclosa.employee.enums.MaritalStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;

@Entity
@Table(name = "employee_personal_info")
@SQLDelete(sql = "UPDATE employee_personal_info SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeePersonalInfo extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false, unique = true)
    private Employee employee;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Gender gender;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "national_id_number", length = 30)
    private String nationalIdNumber;

    @Column(name = "national_id_issue_date")
    private LocalDate nationalIdIssueDate;

    @Column(name = "national_id_issue_place", length = 150)
    private String nationalIdIssuePlace;

    @Column(name = "tax_code", length = 30)
    private String taxCode;

    @Column(name = "social_insurance_number", length = 30)
    private String socialInsuranceNumber;

    @Column(name = "bank_account_number", length = 50)
    private String bankAccountNumber;

    @Column(name = "bank_name", length = 100)
    private String bankName;

    @Column(name = "bank_branch", length = 150)
    private String bankBranch;

    @Enumerated(EnumType.STRING)
    @Column(name = "marital_status", length = 30)
    private MaritalStatus maritalStatus;

    @Column(length = 50)
    @Builder.Default
    private String nationality = "Việt Nam";

    @Column(name = "personal_email", length = 150)
    private String personalEmail;

    @Column(length = 20)
    private String phone;

    @Column(name = "permanent_address", length = 255)
    private String permanentAddress;

    @Column(name = "current_address", length = 255)
    private String currentAddress;

    @Column(name = "photo_url", length = 500)
    private String photoUrl;
}
