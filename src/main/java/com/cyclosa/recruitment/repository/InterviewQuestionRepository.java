package com.cyclosa.recruitment.repository;

import com.cyclosa.recruitment.entity.InterviewQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InterviewQuestionRepository extends JpaRepository<InterviewQuestion, UUID> {
    List<InterviewQuestion> findByInterviewKitIdOrderBySortOrderAsc(UUID interviewKitId);
}
