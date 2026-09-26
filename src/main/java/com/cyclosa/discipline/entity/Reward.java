package com.cyclosa.discipline.entity;

import com.cyclosa.common.entity.BaseEntity;
import com.cyclosa.discipline.enums.RewardType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "rewards")
@SQLDelete(sql = "UPDATE rewards SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reward extends BaseEntity {

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Enumerated(EnumType.STRING)
    @Column(name = "reward_type", nullable = false, length = 40)
    private RewardType rewardType;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "amount", precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "decided_by_employee_id")
    private UUID decidedByEmployeeId;

    @Column(name = "decided_date", nullable = false)
    private LocalDate decidedDate;

    @Column(name = "pushed_to_payroll")
    @Builder.Default
    private Boolean pushedToPayroll = false;
}
