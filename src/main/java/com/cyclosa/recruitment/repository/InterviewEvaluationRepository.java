package com.cyclosa.recruitment.repository;

import com.cyclosa.recruitment.entity.InterviewEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InterviewEvaluationRepository extends JpaRepository<InterviewEvaluation, UUID> {
    List<InterviewEvaluation> findByInterviewId(UUID interviewId);
    Optional<InterviewEvaluation> findByInterviewIdAndInterviewerEmployeeId(UUID interviewId, UUID interviewerEmployeeId);
    boolean existsByInterviewIdAndInterviewerEmployeeId(UUID interviewId, UUID interviewerEmployeeId);
    List<InterviewEvaluation> findByInterviewerEmployeeId(UUID interviewerEmployeeId);
}
