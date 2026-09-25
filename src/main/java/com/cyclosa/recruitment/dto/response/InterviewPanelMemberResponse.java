package com.cyclosa.recruitment.dto.response;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterviewPanelMemberResponse {

    private UUID id;

    private UUID interviewerEmployeeId;

    private String interviewerName;

    private String interviewerEmail;

    private String role;
}
