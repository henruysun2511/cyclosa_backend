package com.cyclosa.contract.entity;

import com.cyclosa.common.entity.BaseEntity;
import com.cyclosa.contract.entity.enums.TemplateFormat;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "contract_template_versions", uniqueConstraints = {
        @UniqueConstraint(name = "uq_template_version", columnNames = {"template_id", "version_no"})
}, indexes = {
        @Index(name = "idx_template_versions_template_id", columnList = "template_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE contract_template_versions SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class ContractTemplateVersion extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private ContractTemplate template;

    @Column(name = "version_no", nullable = false)
    private int versionNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "format", nullable = false, length = 20)
    @Builder.Default
    private TemplateFormat format = TemplateFormat.DOCX;

    @Column(name = "file_url", nullable = false, length = 500)
    private String fileUrl;

    @Column(name = "description", length = 500)
    private String description;
}

