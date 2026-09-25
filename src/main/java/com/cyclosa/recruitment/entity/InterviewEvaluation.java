package com.cyclosa.recruitment.entity;

import com.cyclosa.common.entity.BaseEntity;
import com.cyclosa.recruitment.enums.InterviewRecommendation;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "recruitment_interview_evaluations")
@SQLDelete(sql = "UPDATE recruitment_interview_evaluations SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterviewEvaluation extends BaseEntity {

    @Column(name = "interview_id", nullable = false)
    private UUID interviewId;

    @Column(name = "panel_member_id")
    private UUID panelMemberId;

    @Column(name = "interviewer_employee_id", nullable = false)
    private UUID interviewerEmployeeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "overall_recommendation", nullable = false, length = 30)
    private InterviewRecommendation overallRecommendation;

    @Column(name = "overall_score", precision = 4, scale = 2)
    private BigDecimal overallScore;

    @Column(name = "feedback_data", columnDefinition = "TEXT")
    private String feedbackData;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}
