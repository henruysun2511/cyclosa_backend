package com.cyclosa.contract.entity;

import com.cyclosa.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "contract_versions", indexes = {
        @Index(name = "idx_contract_versions_contract", columnList = "contract_id"),
        @Index(name = "idx_contract_versions_appendix", columnList = "appendix_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE contract_versions SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class ContractVersion extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;

    @Column(name = "appendix_id")
    private UUID appendixId;

    @Column(name = "version_no", nullable = false)
    @Builder.Default
    private int versionNo = 1;

    @Column(name = "file_url", nullable = false, length = 500)
    private String fileUrl;

    @Column(name = "sha256_hash", nullable = false, length = 64)
    private String sha256Hash;

    @Column(name = "change_reason", length = 500)
    private String changeReason;

    @OneToMany(mappedBy = "contractVersion", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ContractSigner> signers = new ArrayList<>();
}

