package com.cyclosa.recruitment.dto.response;

import com.cyclosa.recruitment.enums.CandidateSource;
import com.cyclosa.recruitment.enums.CandidateStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidateResponse {
    private UUID id;
    private UUID companyId;
    private String fullName;
    private String email;
    private String phone;
    private CandidateSource source;
    private CandidateStatus status;
    private String notes;
    private LocalDateTime createdAt;
}
