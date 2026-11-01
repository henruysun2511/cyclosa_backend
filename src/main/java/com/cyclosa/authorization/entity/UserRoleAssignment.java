package com.cyclosa.authorization.entity;

import com.cyclosa.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "user_role_assignments", indexes = {
        @Index(name = "idx_user_role_assignments_user", columnList = "user_id"),
        @Index(name = "idx_user_role_assignments_role", columnList = "role_id"),
        @Index(name = "idx_user_role_assignments_company", columnList = "company_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_role_company", columnNames = {"user_id", "role_id", "company_id"})
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRoleAssignment extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;
}

