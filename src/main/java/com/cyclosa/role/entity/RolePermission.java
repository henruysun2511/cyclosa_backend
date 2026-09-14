package com.cyclosa.role.entity;

import com.cyclosa.common.entity.BaseEntity;
import com.cyclosa.common.enums.DataScope;
import com.cyclosa.permission.entity.Permission;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "role_permissions", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"role_id", "permission_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RolePermission extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "permission_id", nullable = false)
    private Permission permission;

    @Enumerated(EnumType.STRING)
    @Column(name = "data_scope", nullable = false, length = 30)
    @Builder.Default
    private DataScope dataScope = DataScope.OWN;
}
