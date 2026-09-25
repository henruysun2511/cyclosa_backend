package com.cyclosa.recruitment.dto.response;

import com.cyclosa.recruitment.enums.ApplicationStage;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationDetailResponse {
    private UUID id;
    private UUID companyId;
    private UUID candidateId;
    private String candidateName;
    private String candidateEmail;
    private String candidatePhone;
    private UUID jobPositionId;
    private String jobPositionTitle;
    private UUID jobPostingId;
    private ApplicationStage stage;
    private String resumeUrl;
    private String coverLetter;
    private String rejectionReason;
    private BigDecimal scoreMatch;
    private LocalDateTime appliedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
