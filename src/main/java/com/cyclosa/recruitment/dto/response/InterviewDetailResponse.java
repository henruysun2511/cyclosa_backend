package com.cyclosa.recruitment.dto.response;

import com.cyclosa.recruitment.enums.InterviewStatus;
import com.cyclosa.recruitment.enums.InterviewType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterviewDetailResponse {

    private UUID id;

    private UUID companyId;

    private UUID applicationId;

    private String candidateName;

    private String candidateEmail;

    private String candidatePhone;

    private String jobPositionTitle;

    private UUID interviewKitId;

    private String interviewKitTitle;

    private InterviewType interviewType;

    private Integer roundNumber;

    private LocalDateTime scheduledStartTime;

    private LocalDateTime scheduledEndTime;

    private String locationOrMeetingUrl;

    private InterviewStatus status;

    private String cancellationReason;

    private String notes;

    private List<InterviewPanelMemberResponse> panelMembers;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
