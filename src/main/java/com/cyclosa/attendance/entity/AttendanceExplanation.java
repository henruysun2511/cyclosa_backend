package com.cyclosa.attendance.entity;

import com.cyclosa.attendance.enums.ExplanationReasonType;
import com.cyclosa.attendance.enums.ExplanationStatus;
import com.cyclosa.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "attendance_explanations", indexes = {
        @Index(name = "idx_att_exp_emp_date", columnList = "company_id, employee_id, work_date"),
        @Index(name = "idx_att_exp_wf_instance", columnList = "workflow_instance_id")
})
@SQLDelete(sql = "UPDATE attendance_explanations SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceExplanation extends BaseEntity {

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "attendance_record_id")
    private UUID attendanceRecordId;

    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason_type", nullable = false, length = 30)
    private ExplanationReasonType reasonType;

    @Column(name = "proposed_check_in")
    private LocalTime proposedCheckIn;

    @Column(name = "proposed_check_out")
    private LocalTime proposedCheckOut;

    @Column(name = "reason", nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Column(name = "proof_url", length = 500)
    private String proofUrl;

    @Column(name = "workflow_instance_id")
    private UUID workflowInstanceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private ExplanationStatus status = ExplanationStatus.PENDING;
}
