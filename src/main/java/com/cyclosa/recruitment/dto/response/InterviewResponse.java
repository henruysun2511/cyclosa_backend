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
public class InterviewResponse {

    private UUID id;

    private UUID companyId;

    private UUID applicationId;

    private String candidateName;

    private String jobPositionTitle;

    private UUID interviewKitId;

    private InterviewType interviewType;

    private Integer roundNumber;

    private LocalDateTime scheduledStartTime;

    private LocalDateTime scheduledEndTime;

    private String locationOrMeetingUrl;

    private InterviewStatus status;

    private List<InterviewPanelMemberResponse> panelMembers;

    private LocalDateTime createdAt;
}
