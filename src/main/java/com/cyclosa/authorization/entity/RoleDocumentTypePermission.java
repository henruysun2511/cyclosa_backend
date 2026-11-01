package com.cyclosa.authorization.entity;

import com.cyclosa.common.entity.BaseEntity;
import com.cyclosa.common.enums.DataScope;
import com.cyclosa.authorization.enums.DocumentAction;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "role_document_type_permissions", indexes = {
        @Index(name = "idx_role_doc_type_perms_role", columnList = "role_id"),
        @Index(name = "idx_role_doc_type_perms_doc_type", columnList = "document_type_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleDocumentTypePermission extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(name = "document_type_id", nullable = false)
    private UUID documentTypeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 30)
    private DocumentAction action;

    @Enumerated(EnumType.STRING)
    @Column(name = "data_scope", nullable = false, length = 30)
    @Builder.Default
    private DataScope dataScope = DataScope.OWN;
}

