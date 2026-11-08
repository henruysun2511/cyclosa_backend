package com.cyclosa.contract.entity;

import com.cyclosa.common.entity.BaseEntity;
import com.cyclosa.contract.entity.enums.ContractType;
import com.cyclosa.contract.entity.enums.TemplateKind;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "contract_templates", indexes = {
        @Index(name = "idx_contract_templates_company", columnList = "company_id"),
        @Index(name = "idx_contract_templates_kind", columnList = "template_kind"),
        @Index(name = "idx_contract_templates_type", columnList = "contract_type")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE contract_templates SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class ContractTemplate extends BaseEntity {

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "template_kind", nullable = false, length = 30)
    @Builder.Default
    private TemplateKind templateKind = TemplateKind.LABOR_CONTRACT;

    @Enumerated(EnumType.STRING)
    @Column(name = "contract_type", length = 30)
    private ContractType contractType;

    @Column(name = "job_title_id")
    private UUID jobTitleId;

    @Column(name = "department_id")
    private UUID departmentId;

    @Column(name = "branch_id")
    private UUID branchId;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ContractTemplateVersion> versions = new ArrayList<>();
}

