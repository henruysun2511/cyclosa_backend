package com.cyclosa.recruitment.dto.response;

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
public class InterviewKitResponse {

    private UUID id;

    private UUID companyId;

    private UUID jobPositionId;

    private String jobPositionTitle;

    private InterviewType interviewType;

    private String title;

    private String description;

    private List<InterviewQuestionResponse> questions;

    private LocalDateTime createdAt;
}
