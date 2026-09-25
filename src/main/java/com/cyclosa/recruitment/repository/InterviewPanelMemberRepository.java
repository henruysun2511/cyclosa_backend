package com.cyclosa.recruitment.repository;

import com.cyclosa.recruitment.entity.InterviewPanelMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InterviewPanelMemberRepository extends JpaRepository<InterviewPanelMember, UUID> {
    List<InterviewPanelMember> findByInterviewId(UUID interviewId);
    Optional<InterviewPanelMember> findByInterviewIdAndInterviewerEmployeeId(UUID interviewId, UUID interviewerEmployeeId);
    boolean existsByInterviewIdAndInterviewerEmployeeId(UUID interviewId, UUID interviewerEmployeeId);
    void deleteByInterviewIdAndInterviewerEmployeeId(UUID interviewId, UUID interviewerEmployeeId);
}
