package com.cyclosa.recruitment.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TalentPoolResponse {
    private UUID id;
    private UUID companyId;
    private UUID candidateId;
    private String candidateName;
    private String candidateEmail;
    private String candidatePhone;
    private String tag;
    private String notes;
    private LocalDateTime createdAt;
}
