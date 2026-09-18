package com.cyclosa.contract.entity;

import com.cyclosa.common.entity.BaseEntity;
import com.cyclosa.contract.enums.TerminationGround;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "contract_terminations")
@SQLDelete(sql = "UPDATE contract_terminations SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContractTermination extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contract_id", nullable = false, unique = true)
    private Contract contract;

    @Enumerated(EnumType.STRING)
    @Column(name = "termination_ground", nullable = false, length = 40)
    private TerminationGround terminationGround;

    @Column(name = "decision_number", length = 100)
    private String decisionNumber;

    @Column(name = "notice_date", nullable = false)
    private LocalDate noticeDate;

    @Column(name = "final_working_date", nullable = false)
    private LocalDate finalWorkingDate;

    /**
     * Trợ cấp thôi việc tính theo Điều 46 BLLĐ 2019 (0.5 tháng lương/năm).
     */
    @Column(name = "severance_allowance", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal severanceAllowance = BigDecimal.ZERO;

    /**
     * Trợ cấp mất việc làm tính theo Điều 47 BLLĐ 2019 (1 tháng lương/năm, tối thiểu 2 tháng).
     */
    @Column(name = "loss_of_work_allowance", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal lossOfWorkAllowance = BigDecimal.ZERO;

    /**
     * Tiền thanh toán những ngày phép năm chưa nghỉ hết (Điều 113.3).
     */
    @Column(name = "remaining_leave_pay", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal remainingLeavePay = BigDecimal.ZERO;

    /**
     * Tiền bồi thường (nếu vi phạm thời hạn báo trước hoặc đơn phương trái luật).
     */
    @Column(name = "compensation_amount", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal compensationAmount = BigDecimal.ZERO;

    @Column(name = "is_unlawful_termination", nullable = false)
    @Builder.Default
    private boolean unlawfulTermination = false;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "approved_by_employee_id")
    private UUID approvedByEmployeeId;

    @Column(name = "signed_decision_url", length = 500)
    private String signedDecisionUrl;
}
