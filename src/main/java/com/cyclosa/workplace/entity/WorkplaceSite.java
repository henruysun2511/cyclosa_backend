package com.cyclosa.workplace.entity;

import com.cyclosa.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.UUID;

@Entity
@Table(name = "workplace_sites")
@SQLDelete(sql = "UPDATE workplace_sites SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkplaceSite extends BaseEntity {

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "branch_id", nullable = false)
    private UUID branchId;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
}
