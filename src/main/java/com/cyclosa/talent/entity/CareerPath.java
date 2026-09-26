package com.cyclosa.talent.entity;

import com.cyclosa.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "career_paths")
@SQLDelete(sql = "UPDATE career_paths SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CareerPath extends BaseEntity {

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "from_position_id", nullable = false)
    private UUID fromPositionId;

    @Column(name = "to_position_id", nullable = false)
    private UUID toPositionId;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "min_years_required", precision = 4, scale = 1)
    private BigDecimal minYearsRequired;
}
