package com.cyclosa.recruitment.dto.response;

import com.cyclosa.recruitment.enums.OfferStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OfferDetailResponse {

    private UUID id;

    private UUID companyId;

    private UUID applicationId;

    private String candidateName;

    private String candidateEmail;

    private String candidatePhone;

    private String jobPositionTitle;

    private BigDecimal offeredSalary;

    private LocalDate startDate;

    private LocalDate expirationDate;

    private String benefitsNotes;

    private OfferStatus status;

    private String declineReason;

    private String notes;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
