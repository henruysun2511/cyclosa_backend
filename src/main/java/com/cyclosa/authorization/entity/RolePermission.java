package com.cyclosa.authorization.entity;

import com.cyclosa.common.entity.BaseEntity;
import com.cyclosa.common.enums.DataScope;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "role_permissions", indexes = {
        @Index(name = "idx_role_permissions_role_id", columnList = "role_id"),
        @Index(name = "idx_role_permissions_permission_id", columnList = "permission_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_role_permissions_role_perm", columnNames = {"role_id", "permission_id"})
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RolePermission extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permission_id", nullable = false)
    private Permission permission;

    @Enumerated(EnumType.STRING)
    @Column(name = "data_scope", nullable = false, length = 30)
    @Builder.Default
    private DataScope dataScope = DataScope.OWN;
}

