package com.cyclosa.talent.entity;

import com.cyclosa.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.UUID;

@Entity
@Table(name = "internal_talent_pool")
@SQLDelete(sql = "UPDATE internal_talent_pool SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InternalTalentPool extends BaseEntity {

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "tag", nullable = false, length = 100)
    private String tag;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @Column(name = "added_by_employee_id")
    private UUID addedByEmployeeId;
}
