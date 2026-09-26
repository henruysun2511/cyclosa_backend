package com.cyclosa.onboarding.entity;

import com.cyclosa.common.entity.BaseEntity;
import com.cyclosa.onboarding.enums.ProvisioningStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "account_provisioning")
@SQLDelete(sql = "UPDATE account_provisioning SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountProvisioning extends BaseEntity {

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "onboarding_process_id")
    private UUID onboardingProcessId;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "system_name", nullable = false, length = 100)
    private String systemName;

    @Column(name = "account_username", nullable = false, length = 150)
    private String accountUsername;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private ProvisioningStatus status = ProvisioningStatus.PENDING;

    @Column(name = "provisioned_by_employee_id")
    private UUID provisionedByEmployeeId;

    @Column(name = "provisioned_at")
    private LocalDateTime provisionedAt;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}
