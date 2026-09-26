package com.cyclosa.talent.entity;

import com.cyclosa.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "career_simulation_saved_paths")
@SQLDelete(sql = "UPDATE career_simulation_saved_paths SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CareerSimulationSavedPath extends BaseEntity {

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "target_position_id")
    private UUID targetPositionId;

    @Column(name = "suggested_path", columnDefinition = "TEXT")
    private String suggestedPath;

    @Column(name = "saved_at", nullable = false)
    private LocalDateTime savedAt;
}
